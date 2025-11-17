Feature: Event Processing
  As the notification orchestrator
  I want to process user events
  So that appropriate notifications are sent through the correct channels

  Scenario: Process user registration event
    Given a new user registers with email "john@example.com"
    When the USER_REGISTERED event is received
    Then a welcome email notification should be queued
    And the notification should include the user's name
    And the event should be saved in the database

  Scenario: Process user login event
    Given a user logs in with email "alice@example.com" and phone "1234567890"
    When the USER_LOGIN event is received
    Then a login alert email should be queued
    And a login alert SMS should be queued
    And both notifications should be saved in the database

  Scenario: Process password reset request
    Given a user requests password reset for email "bob@example.com"
    When the PASSWORD_RESET_REQUESTED event is received
    Then a password reset email should be queued
    And the email should contain a reset link
    And the dispatch request should be saved

  Scenario: Process password changed event
    Given a user changes password for email "jane@example.com" and phone "9876543210"
    When the PASSWORD_CHANGED event is received
    Then a password changed email should be queued
    And a password changed SMS should be queued
    And both dispatch requests should be recorded
