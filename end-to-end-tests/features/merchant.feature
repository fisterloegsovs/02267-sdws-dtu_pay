Feature: Merchant Registration and Deregistration

  Scenario: Registering a merchant
    Given a merchant with a bank account
    When the merchant registers with DTU Pay
    Then the merchant is successfully registered

  Scenario: Deregistering a merchant
    Given a merchant with a bank account
    When the merchant registers with DTU Pay
    And the merchant deregisters from DTU Pay
    Then the merchant is successfully deregistered

  Scenario: Successful payment from customer to merchant
    Given a customer with a valid bank account with sufficient amount and valid token
    And a merchant with a valid bank account
    When the merchant initiates a payment request with the customer's token
    Then the payment is successfully processed
    And the merchant receives the amount
    And the customer's balance is reduced by the payment amount

  Scenario: Successful payment from customer to merchant and merchant can generate transaction report
    Given a customer with a valid bank account with sufficient amount and valid token
    And a merchant with a valid bank account
    When the merchant initiates a payment request with the customer's token
    Then the payment is successfully processed
    And the merchant receives the amount
    And the customer's balance is reduced by the payment amount
    And the merchant can generate a transaction report


