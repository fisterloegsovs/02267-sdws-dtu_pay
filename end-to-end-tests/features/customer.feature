Feature: Payment

# Author: Jonas Puidokas (137282)
Scenario: Customer registration
	Given a customer with a bank account
	When the customer is registered
	Then a customer id is returned
	#And the customer has 1 token

# Author: Jonas Puidokas (137282)
Scenario: Customer deregistration
	Given a customer with a bank account
	When the customer is registered
	And the customer is deregistered
	Then a customer does not exist

# Author: Jonas Puidokas (137282)
Scenario: Customer token creation
	Given a customer with a bank account
	When the customer is registered
	And the customer requests 1 token
	Then a customer has 1 token
