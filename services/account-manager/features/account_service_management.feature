Feature: Account Service Management

#  # Author: Jonas Puidokas (137282)
#  Scenario: Register a customer
#    Given a valid customer with details cpr "1234567890", first name "Alice", last name "Smith", account "customer-account", and address "customer-address"
#    When the customer registration is initiated
#    Then the customer is successfully registered

  # Author: Jonas Puidokas (137282)
  Scenario: Deregister a customer
    Given a customer with ID "3f5a0e4b-7c2d-4f1e-9b6a-5d8c3e2f1a9d"
    When the customer deregistration is initiated
    Then the customer is successfully deregistered

  Scenario: Register a merchant
    Given a valid merchant with details cpr "1234567890", first name "Alice", last name "Smith", account "merchant-account", and address "merchant-address"
    When the merchant registration is initiated
    Then the merchant is successfully registered

  Scenario: Deregister a merchant
    Given a merchant with ID "123e4567-e89b-12d3-a456-426614174001"
    When the merchant deregistration is initiated
    Then the merchant is successfully deregistered
