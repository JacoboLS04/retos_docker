Feature: Health Check Endpoints
  As a system administrator
  I want to monitor the service health status
  So that I can ensure the orchestrator is running correctly

  Scenario: Check overall health when service is ready and alive
    Given the notification orchestrator is ready
    And the notification orchestrator is alive
    When I request the health status
    Then the health status should be "UP"
    And the readiness check should be "UP"
    And the liveness check should be "UP"

  Scenario: Check readiness endpoint when service is ready
    Given the notification orchestrator is ready
    When I check the readiness endpoint
    Then the readiness status should be 200
    And the readiness should indicate "READY"

  Scenario: Check liveness endpoint when service is alive
    Given the notification orchestrator is alive
    When I check the liveness endpoint
    Then the liveness status should be 200
    And the liveness should indicate "ALIVE"

  Scenario: Health check returns DOWN when service is not ready
    Given the notification orchestrator is not ready
    And the notification orchestrator is alive
    When I request the health status
    Then the health status should be "DOWN"
    And the readiness check should be "DOWN"
