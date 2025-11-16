Feature: Authenticated Greeting Service
  As an authenticated user
  I want to receive a personalized greeting
  So that I can verify my identity is recognized by the system

  Scenario: Greet authenticated user with matching name
    Given I am authenticated as user "john_doe"
    When I request a greeting with name "john_doe"
    Then I should receive a successful greeting response
    And the greeting should say "Hola john_doe"

  Scenario: Reject greeting when name does not match token
    Given I am authenticated as user "alice"
    When I request a greeting with name "bob"
    Then the request should be forbidden
    And the error should indicate name mismatch with token

  Scenario: Reject greeting request without name parameter
    Given I am authenticated as user "jane"
    When I request a greeting without providing a name
    Then the request should fail with bad request error
    And the error should indicate name is required

  Scenario: Handle unauthenticated requests
    Given I am not authenticated
    When I attempt to request a greeting
    Then the request should be unauthorized
