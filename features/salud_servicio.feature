Feature: Verify API Gateway service status
  As a system administrator
  I want to verify that the service is functioning correctly
  So that I can ensure users can access the functionalities

  Scenario: The service is active and responds correctly
    Given the API Gateway service is running
    When I check the service health status
    Then the service should respond that it is functioning correctly
    And the status should be "ok"
