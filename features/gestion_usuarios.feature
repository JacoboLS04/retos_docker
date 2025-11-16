Feature: User Information Management
  As a system user
  I want to be able to view, update, and delete my personal information
  So that I can keep my data up to date and manage my privacy

  Scenario: View complete user information
    Given a user with identifier "user-123" exists in the system
    When I request to view that user's complete information
    Then I should receive their personal data
    And I should receive their profile information

  Scenario: Update a user's profile information
    Given a user with identifier "user-456" exists in the system
    When I update their biography to "Software developer"
    Then the information should be saved correctly
    And the user's biography should be "Software developer"

  Scenario: Delete a user from the system
    Given a user with identifier "user-789" exists in the system
    When I request to delete that user from the system
    Then the user should be deleted successfully
    And the user's deletion should be notified
