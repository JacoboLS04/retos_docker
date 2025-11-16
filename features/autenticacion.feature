Feature: User Authentication
  As a system user
  I want to be able to register and log in
  So that I can access protected functionalities

  Scenario: Register a new user
    Given I want to create a new account
    When I provide my username "juan_perez" and password
    Then my account should be created successfully
    And I should receive my user identifier

  Scenario: Log in with valid credentials
    Given I have a registered account with username "maria_garcia"
    When I log in with my username and correct password
    Then I should receive an access token
    And the system should confirm my identity
