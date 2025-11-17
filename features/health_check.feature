Feature: Health Check Endpoints
  As a system administrator
  I want to monitor the notification service health
  So that I can ensure the service is running correctly

  Scenario: Check overall service health
    When I request the service health status
    Then the health status should be "UP"
    And the response should include readiness check
    And the response should include liveness check

  Scenario: Check service readiness
    When I check the readiness endpoint
    Then the readiness status should be "UP"
    And the response should include uptime information

  Scenario: Check service liveness
    When I check the liveness endpoint
    Then the liveness status should be "UP"
    And the response should confirm the service is alive

  Scenario: Health check includes version information
    When I request the service health status
    Then the response should include version number
    And the response should include uptime in human readable format
