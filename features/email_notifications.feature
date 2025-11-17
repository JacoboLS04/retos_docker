Feature: Email Notification Service
  As a notification system
  I want to send email notifications
  So that users receive important information via email

  Scenario: Send welcome email successfully
    Given the email service is available
    When I send a welcome email to "user@example.com"
    Then the email should be sent successfully
    And the notification log should record the email as "SENT"

  Scenario: Send password reset email
    Given the email service is available
    When I send a password reset email to "user@example.com"
    Then the email should contain a reset link
    And the notification should be logged

  Scenario: Handle email service failure
    Given the email service is unavailable
    When I attempt to send an email to "user@example.com"
    Then the email should fail to send
    And the notification log should record the error

  Scenario: Send email with HTML content
    Given the email service is available
    When I send an HTML email to "user@example.com" with content "<h1>Hello</h1>"
    Then the email should be sent successfully
    And the content should be HTML formatted
