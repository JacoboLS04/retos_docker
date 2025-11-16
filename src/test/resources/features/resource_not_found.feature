Feature: Handle Unknown Resources
  As a system
  I want to handle requests to unknown endpoints gracefully
  So that users receive clear feedback when accessing invalid routes

  Scenario: Access non-existent endpoint
    Given I am authenticated as user "testuser"
    When I access an unknown endpoint "/unknown-path"
    Then the response should indicate resource not found
    And the status should be 404

  Scenario: Access root path that doesn't exist
    Given I am authenticated as user "admin"
    When I access the root path "/"
    Then the response should indicate resource not found
    And the status should be 404
