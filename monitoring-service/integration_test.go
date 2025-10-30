//go:build integration
// +build integration

package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"net/http"
	"os"
	"testing"
	"time"
)

const testPort = "8086"

func TestIntegration_ServiceLifecycle(t *testing.T) {
	// Start the server in a goroutine
	os.Setenv("PORT", testPort)
	defer os.Unsetenv("PORT")

	tmpDir, err := ioutil.TempDir("", "monitoring-test")
	if err != nil {
		t.Fatal(err)
	}
	defer os.RemoveAll(tmpDir)

	targetFile := tmpDir + "/targets.json"
	os.Setenv("TARGETS_FILE", targetFile)
	defer os.Unsetenv("TARGETS_FILE")

	go main()
	time.Sleep(1 * time.Second) // Give the server time to start

	baseURL := fmt.Sprintf("http://localhost:%s", testPort)

	// 1. Register a service
	t.Run("register service", func(t *testing.T) {
		service := Service{
			Name: "test-integration",
			URL:  "http://test.local/health",
		}
		body, _ := json.Marshal(service)

		resp, err := http.Post(baseURL+"/register", "application/json", bytes.NewBuffer(body))
		if err != nil {
			t.Fatal(err)
		}
		defer resp.Body.Close()

		if resp.StatusCode != http.StatusCreated {
			t.Errorf("Expected status Created, got %v", resp.Status)
		}

		// Verify targets.json was created
		if _, err := os.Stat(targetFile); os.IsNotExist(err) {
			t.Error("targets.json was not created")
		}
	})

	// Esperar un momento para que las métricas se actualicen
	time.Sleep(100 * time.Millisecond)

	// 2. List services
	t.Run("list services", func(t *testing.T) {
		resp, err := http.Get(baseURL + "/services")
		if err != nil {
			t.Fatal(err)
		}
		defer resp.Body.Close()

		if resp.StatusCode != http.StatusOK {
			t.Errorf("Expected status OK, got %v", resp.Status)
		}

		var services map[string]interface{}
		if err := json.NewDecoder(resp.Body).Decode(&services); err != nil {
			t.Fatal(err)
		}

		if _, exists := services["test-integration"]; !exists {
			t.Error("test-integration service not found in list")
		}
	})

	// 3. Check metrics endpoint
	t.Run("metrics endpoint", func(t *testing.T) {
		resp, err := http.Get(baseURL + "/metrics")
		if err != nil {
			t.Fatal(err)
		}
		defer resp.Body.Close()

		if resp.StatusCode != http.StatusOK {
			t.Errorf("Expected status OK, got %v", resp.Status)
		}

		body, _ := ioutil.ReadAll(resp.Body)
		
		if !bytes.Contains(body, []byte("service_up")) {
			t.Error("metrics missing service_up")
		}
		if !bytes.Contains(body, []byte("service_last_check_timestamp")) {
			t.Error("metrics missing service_last_check_timestamp")
		}
	})

	// 4. Test alert webhook
	t.Run("alert webhook", func(t *testing.T) {
		alert := map[string]interface{}{
			"version": "4",
			"status":  "firing",
			"alerts": []map[string]interface{}{
				{
					"status": "firing",
					"labels": map[string]interface{}{
						"alertname": "TestAlert",
						"service":   "test-integration",
					},
				},
			},
		}
		body, _ := json.Marshal(alert)

		resp, err := http.Post(baseURL+"/alert", "application/json", bytes.NewBuffer(body))
		if err != nil {
			t.Fatal(err)
		}
		defer resp.Body.Close()

		if resp.StatusCode != http.StatusOK {
			t.Errorf("Expected status OK, got %v", resp.Status)
		}
	})
}
