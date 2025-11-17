Feature: SMS Notification Service
  As a notification system
  I want to send SMS notifications
  So that users receive urgent alerts on their mobile phones

  Scenario: Send verification code via SMS
    Given the SMS service is available
    When I send a verification code SMS to "+1234567890"
    Then the SMS should be delivered successfully
    And the notification log should record the SMS as "SENT"

  Scenario: Send login alert via SMS
    Given the SMS service is available
    When I send a login alert SMS to "+9876543210"
    Then the SMS should be delivered successfully
    And the message should indicate a new login

  Scenario: Reject SMS with invalid phone number
    Given the SMS service is available
    When I attempt to send SMS to an empty phone number
    Then the SMS should not be sent
    And the notification log should record "FAILED" status

  Scenario: Handle SMS service failure
    Given the SMS service is unavailable
    When I attempt to send an SMS to "+1234567890"
    Then the SMS should fail to send
    And the error should be logged in the notification log
