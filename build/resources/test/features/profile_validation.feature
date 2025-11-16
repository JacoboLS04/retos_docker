Feature: Profile Data Validation
  As a system
  I want to validate profile data
  So that only valid information is stored

  Scenario: Create profile with invalid URL
    Given I am authenticated as user with ID 10
    When I attempt to create a profile with invalid page URL "not-a-valid-url"
    Then the request should fail with validation error
    And the error should indicate invalid URL format

  Scenario: Create profile with very long bio
    Given I am authenticated as user with ID 11
    When I attempt to create a profile with bio exceeding 2000 characters
    Then the request should fail with validation error
    And the error should indicate bio is too long

  Scenario: Create duplicate profile
    Given I am authenticated as user with ID 12
    And I already have an existing profile
    When I attempt to create another profile
    Then the request should fail with conflict error
    And the error should indicate profile already exists
