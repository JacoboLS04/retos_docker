Monitoring Service (Go)

This small service allows registering microservices to be monitored by Prometheus/Blackbox.

Features:
- POST /register {"name":"svc","url":"http://svc:8080/health"}
- GET /services
- /metrics Prometheus metrics (service_up, service_last_check_timestamp)
- POST /alert Alertmanager webhook receiver (forwards to notifications service)

Docker:
- writes file_sd targets to `/out/targets.json` (mount this to Prometheus `/etc/prometheus/file_sd`).

Env:
- PORT (default 8085)
- TARGETS_FILE (default /out/targets.json)
- NOTIFICATIONS_ENDPOINT (optional) to forward Alertmanager webhooks

## Tests and Development

### Windows Users
For Windows users, we provide a `test.bat` script to run tests. This script provides the same functionality as Make but is compatible with Windows systems:

```powershell
# Run all tests
.\test.bat test

# Run only unit tests
.\test.bat test-unit

# Run only integration tests
.\test.bat test-integration

# Run the service
.\test.bat run

# Build the service
.\test.bat build
```

### Unix/Linux Users (Make)
For Unix/Linux users, we use Make to simplify and standardize development tasks. Make is a build automation tool that helps manage project tasks efficiently:

```bash
# Run all tests (both unit and integration)
make test

# Run only unit tests
make test-unit

# Run only integration tests
make test-integration

# Run the service
make run

# Build the service
make build
```

### Why Make?
We use Make in this project for several reasons:

1. **Standardization**: Provides a consistent interface for common development tasks across different environments.
2. **Automation**: Automates repetitive tasks and complex test scenarios.
3. **Dependencies**: Manages build dependencies and ensures proper execution order.
4. **Efficiency**: Reduces the need to remember long command sequences.

### Test Types

1. **Unit Tests**
   - Test individual components in isolation
   - Located in `*_test.go` files
   - Don't require external services

2. **Integration Tests**
   - Test complete service workflows
   - Located in `integration_test.go`
   - Use build tag `integration`
   - Test actual HTTP endpoints and Prometheus metrics
