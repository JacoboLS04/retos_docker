Feature: Simple arithmetic

  Scenario: Add two numbers
    Given two numbers 2 and 3
    When I add them
    Then the result should be 5
