package main

import (
	"bytes"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"
	"time"
)

func TestRegisterHandler(t *testing.T) {
	tests := []struct {
		name       string
		service    Service
		wantStatus int
	}{
		{
			name:       "valid service registration",
			service:    Service{Name: "test-service", URL: "http://test.com"},
			wantStatus: http.StatusCreated,
		},
		{
			name:       "invalid service - missing name",
			service:    Service{URL: "http://test.com"},
			wantStatus: http.StatusBadRequest,
		},
		{
			name:       "invalid service - missing url",
			service:    Service{Name: "test-service"},
			wantStatus: http.StatusBadRequest,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			// Clear services map before each test
			mu.Lock()
			services = map[string]*RegService{}
			mu.Unlock()

			body, _ := json.Marshal(tt.service)
			req := httptest.NewRequest("POST", "/register", bytes.NewBuffer(body))
			w := httptest.NewRecorder()

			registerHandler(w, req)

			if w.Code != tt.wantStatus {
				t.Errorf("registerHandler() status = %v, want %v", w.Code, tt.wantStatus)
			}

			if tt.wantStatus == http.StatusCreated {
				mu.RLock()
				if _, exists := services[tt.service.Name]; !exists {
					t.Error("service was not registered")
				}
				mu.RUnlock()
			}
		})
	}
}

func TestListHandler(t *testing.T) {
	// Setup test services
	mu.Lock()
	services = map[string]*RegService{
		"test-service": {
			svc:      Service{Name: "test-service", URL: "http://test.com"},
			lastUp:   1,
			lastTime: time.Now(),
		},
	}
	mu.Unlock()

	req := httptest.NewRequest("GET", "/services", nil)
	w := httptest.NewRecorder()

	listHandler(w, req)

	if w.Code != http.StatusOK {
		t.Errorf("listHandler() status = %v, want %v", w.Code, http.StatusOK)
	}

	var response map[string]interface{}
	if err := json.NewDecoder(w.Body).Decode(&response); err != nil {
		t.Errorf("Failed to decode response: %v", err)
	}

	if len(response) != 1 {
		t.Errorf("Expected 1 service, got %d", len(response))
	}

	if _, exists := response["test-service"]; !exists {
		t.Error("test-service not found in response")
	}
}

func TestRunChecks(t *testing.T) {
	// Create a test server that will respond to health checks
	ts := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))
	defer ts.Close()

	// Register a test service using the test server URL
	mu.Lock()
	services = map[string]*RegService{
		"test-service": {
			svc:      Service{Name: "test-service", URL: ts.URL},
			lastUp:   0,
			lastTime: time.Time{},
		},
	}
	mu.Unlock()

	// Run the health checks
	runChecks()

	// Verify the service status was updated
	mu.RLock()
	defer mu.RUnlock()

	service := services["test-service"]
	if service.lastUp != 1 {
		t.Errorf("Expected service to be up (1), got %d", service.lastUp)
	}
	if service.lastTime.IsZero() {
		t.Error("Expected lastTime to be updated")
	}
}

func TestAlertHandler(t *testing.T) {
	// Create a mock notifications server
	mockNotifs := httptest.NewServer(http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
	}))
	defer mockNotifs.Close()

	// Set the notifications endpoint to our mock server
	os.Setenv("NOTIFICATIONS_ENDPOINT", mockNotifs.URL)
	defer os.Unsetenv("NOTIFICATIONS_ENDPOINT")

	alertPayload := []byte(`{"alerts":[{"status":"firing"}]}`)
	req := httptest.NewRequest("POST", "/alert", bytes.NewBuffer(alertPayload))
	w := httptest.NewRecorder()

	alertHandler(w, req)

	if w.Code != http.StatusOK {
		t.Errorf("alertHandler() status = %v, want %v", w.Code, http.StatusOK)
	}

	// Give some time for the async forward to complete
	time.Sleep(100 * time.Millisecond)
}
