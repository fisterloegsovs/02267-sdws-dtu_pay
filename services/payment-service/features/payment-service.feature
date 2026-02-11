Feature: Payment Service Tests

  Scenario: Valid token and successful payment
    Given a valid token "123e4567-e89b-12d3-a456-426614174000"
    And a valid customer account "customer-account" and a merchant account "merchant-account"
    When the payment is initiated with amount 100.0
    Then a payment succeeded event is published

  Scenario: Invalid token
    Given an invalid token "123e4567-e89b-12d3-a456-426614174000"
    And a valid customer account "customer-account" and a merchant account "merchant-account"
    When the payment is initiated with amount 100.0
    Then a payment failed event is published with reason "Invalid or expired token"

  Scenario: Valid token but failed customer account retrieval
    Given a valid token "123e4567-e89b-12d3-a456-426614174000"
    And a customer account retrieval fails
    When the payment is initiated with amount 100.0
    Then a payment failed event is published with reason "Customer account not found"

  Scenario: Valid token but failed bank transfer
    Given a valid token "123e4567-e89b-12d3-a456-426614174000"
    And a valid customer account "customer-account" and a merchant account "merchant-account"
    And a bank transfer failure is simulated
    When the payment is initiated with amount 100.0
    Then a payment failed event is published with reason "Bank transfer failed without exception."
