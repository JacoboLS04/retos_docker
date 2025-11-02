//go:build e2e
// +build e2e

package e2e

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"
	"testing"
	"time"
)

type service struct {
	Name string `json:"name"`
	URL  string `json:"url"`
}

func getenv(k, def string) string {
	v := os.Getenv(k)
	if v == "" {
		return def
	}
	return v
}

func TestE2E_MonitoringStack(t *testing.T) {
	baseMon := getenv("MONITORING_BASE_URL", "http://localhost:8085")
	baseProm := os.Getenv("PROMETHEUS_BASE_URL") // optional
	targetURL := getenv("TARGET_SERVICE_URL", "http://prometheus:9090/-/healthy")
	serviceName := getenv("TARGET_SERVICE_NAME", "prometheus-health")

	client := &http.Client{Timeout: 10 * time.Second}

	t.Run("monitoring up", func(t *testing.T) {
		resp, err := client.Get(baseMon + "/services")
		if err != nil {
			t.Fatalf("monitoring-service no disponible en %s: %v", baseMon, err)
		}
		resp.Body.Close()
	})

	t.Run("registrar servicio", func(t *testing.T) {
		payload := service{Name: serviceName, URL: targetURL}
		b, _ := json.Marshal(payload)
		resp, err := client.Post(baseMon+"/register", "application/json", bytes.NewReader(b))
		if err != nil {
			t.Fatalf("error registrando servicio: %v", err)
		}
		defer resp.Body.Close()
		if resp.StatusCode != http.StatusCreated && resp.StatusCode != http.StatusOK {
			body, _ := io.ReadAll(resp.Body)
			t.Fatalf("registro devolvió %d: %s", resp.StatusCode, string(body))
		}
	})

	t.Run("metrics refleja service_up=1", func(t *testing.T) {
		deadline := time.Now().Add(60 * time.Second)
		for {
			resp, err := client.Get(baseMon + "/metrics")
			if err == nil {
				data, _ := io.ReadAll(resp.Body)
				resp.Body.Close()
				// Buscar la métrica con el nombre del servicio; evitar dependencia del orden de labels
				needle := fmt.Sprintf("service_up{", serviceName)
				if bytes.Contains(data, []byte("service_up")) && bytes.Contains(data, []byte(serviceName)) && bytes.Contains(data, []byte(" 1\n")) {
					break
				}
			}
			if time.Now().After(deadline) {
				t.Fatalf("no se observó service_up=1 para %s antes del timeout", serviceName)
			}
			time.Sleep(2 * time.Second)
		}
	})

	if baseProm != "" {
		t.Run("Prometheus ve el objetivo", func(t *testing.T) {
			deadline := time.Now().Add(60 * time.Second)
			// Consultar los targets de Prometheus y buscar el URL/host del objetivo
			for {
				resp, err := client.Get(strings.TrimRight(baseProm, "/") + "/api/v1/targets")
				if err == nil {
					body, _ := io.ReadAll(resp.Body)
					resp.Body.Close()
					if bytes.Contains(body, []byte("activeTargets")) && bytes.Contains(body, []byte("prometheus:9090")) {
						break
					}
				}
				if time.Now().After(deadline) {
					t.Fatalf("Prometheus no reporta el target esperado (prometheus:9090) antes del timeout")
				}
				time.Sleep(3 * time.Second)
			}
		})
	}
}
