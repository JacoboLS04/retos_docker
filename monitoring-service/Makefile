.PHONY: test test-unit test-integration

test: test-unit test-integration

test-unit:
	@echo "Running unit tests..."
	go test -v ./... -run "^Test[^Integration]"

test-integration:
	@echo "Running integration tests..."
	go test -v ./... -tags=integration -run "^TestIntegration"

run:
	go run main.go

build:
	go build -o monitoring-service .