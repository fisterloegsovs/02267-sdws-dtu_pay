package cucumber.customer;

import dtu.ws.fastmoney.*;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CustomerRegistrationSteps {

	String cpr = "211091-1150";
	BankService bank = new BankServiceService().getBankServicePort();
	List<String> accounts = new ArrayList<>();
	CustomerFacade customerFacade = new CustomerFacade();
	Customer customer;
	String customerId;
	String token;
	private Response response;

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@Before
	public void prepareBank() throws BankServiceException_Exception {
		try {
			Account bankAccount = bank.getAccountByCprNumber(cpr);
			bank.retireAccount(bankAccount.getId());
		} catch (BankServiceException_Exception e) {
			// Everything ok
		}
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@Given("a customer with a bank account")
	public void aCustomerWithABankAccount() throws BankServiceException_Exception {
		User user = new User();
		user.setCprNumber(cpr);
		user.setFirstName("Niels");
		user.setLastName("Nielsen");
		String bankAccount = bank.createAccountWithBalance(user, new BigDecimal(1000));
		accounts.add(bankAccount);

		customer = new Customer(
			user.getCprNumber().replace("-", ""),
			user.getFirstName(),
			user.getLastName(),
			bankAccount,
			"Vejen 88, Kolding, Denmark");
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@When("the customer is registered")
	public void theCustomerIsRegistered() {
		customerId = customerFacade.register(customer);
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@When("the customer is deregistered")
	public void theCustomerIsDeregistered() {
		response = customerFacade.deregister(customerId);
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@Then("a customer id is returned")
	public void aCustomerIdIsReturned() {
		assertNotNull(customerId);
		assertFalse(customerId.isEmpty());
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@Then("a customer does not exist")
	public void aCustomerDoesNotExist() {
		assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
		try {
			response = customerFacade.deregister(customerId);
		} catch (NotFoundException e) {
			response = Response.status(Response.Status.NOT_FOUND).build();
		}
		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@When("the customer requests {int} token")
	public void customerRequests1Token(int tokenAmount) {
		TokenCreateRequest request = new TokenCreateRequest(customerId, tokenAmount);
		response = customerFacade.createTokens(request);

		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@Then("a customer has {int} token")
	public void customerHas1Token(long tokenAmount) {
		response = customerFacade.getTokens(customerId);
		List<String> tokens = response.readEntity(List.class);
		token = tokens.isEmpty() ? "0" : tokens.getFirst();

		assertEquals(tokenAmount, tokens.size());
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	@After
	public void cleanupAccounts() throws BankServiceException_Exception {
		for (var account : accounts) {
			bank.retireAccount(account);
		}
		customerFacade.deregister(customerId);
	}
}
