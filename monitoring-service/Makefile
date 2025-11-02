.PHONY: test test-unit test-integration test-e2e

test: test-unit test-integration

test-unit:
	@echo "Running unit tests..."
	go test -v ./... -run "^Test[^Integration]"

test-integration:
	@echo "Running integration tests..."
	go test -v ./... -tags=integration -run "^TestIntegration"

# End-to-end tests contra el stack completo (requiere MONITORING_BASE_URL y opcional PROMETHEUS_BASE_URL)
test-e2e:
	@echo "Running end-to-end tests..."
	MONITORING_BASE_URL?=http://localhost:8085 ; \
	PROMETHEUS_BASE_URL?=http://localhost:9090 ; \
	TARGET_SERVICE_URL?=http://prometheus:9090/-/healthy ; \
	go test -v -tags=e2e ./e2e -timeout 2m

run:
	go run main.go

build:
	go build -o monitoring-service .