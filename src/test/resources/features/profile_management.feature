Feature: User Profile Management
  As an authenticated user
  I want to manage my profile information
  So that I can keep my personal data up to date

  Scenario: Create a new profile
    Given I am authenticated as user with ID 1
    When I create a profile with nickname "john_dev" and bio "Software developer"
    Then the profile should be created successfully
    And the profile should have nickname "john_dev"
    And the profile should have bio "Software developer"

  Scenario: View my profile
    Given I am authenticated as user with ID 2
    And I have an existing profile with nickname "jane_designer"
    When I request to view my profile
    Then I should receive my profile information
    And the profile should contain my nickname "jane_designer"

  Scenario: Update profile information
    Given I am authenticated as user with ID 3
    And I have an existing profile with bio "Old bio"
    When I update my profile with new bio "Updated bio text"
    Then the profile should be updated successfully
    And the bio should be "Updated bio text"

  Scenario: Update profile partially
    Given I am authenticated as user with ID 4
    And I have an existing profile with country "Colombia"
    When I partially update only the country to "Argentina"
    Then the profile should be updated successfully
    And the country should be "Argentina"

  Scenario: Delete my profile
    Given I am authenticated as user with ID 5
    And I have an existing profile with nickname "to_delete"
    When I request to delete my profile
    Then the profile should be deleted successfully
    And the profile should no longer exist
