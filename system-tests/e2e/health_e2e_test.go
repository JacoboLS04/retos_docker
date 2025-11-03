//go:build e2e
// +build e2e

package e2e

import (
	"testing"
	"time"

	"retos_observabilidad/system-tests/internal/testutil"
)

func Test_Health_AllServices(t *testing.T) {
	// Estos endpoints son accesibles desde la red Docker 'retos-net'
	spring := getenv("SPRING_READY_URL", "http://retos-spring-app:8080/health/ready")
	orch := getenv("ORCH_READY_URL", "http://orchestrator:8080/health")
	ms := getenv("MS_NOTIF_READY_URL", "http://ms-notifications:8080/health")

	for name, url := range map[string]string{
		"spring": spring,
		"orchestrator": orch,
		"ms-notifications": ms,
	} {
		url := url
		t.Run(name, func(t *testing.T) {
			t.Helper()
			if err := testutil.HTTPGetOK(url, 2*time.Minute); err != nil {
				t.Fatalf("%s no responde healthy: %v", name, err)
			}
		})
	}
}
