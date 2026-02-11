package cucumber.customer;

import cucumber.merchant.models.PaymentRequest;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class CustomerFacade {

	WebTarget baseUrl;

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public CustomerFacade() {
		Client client = ClientBuilder.newClient();
		baseUrl = client.target("http://localhost:8080/");
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public String register(Customer customer) {
		return baseUrl.path("customer/register")
				.request()
				.post(Entity.entity(customer, MediaType.APPLICATION_JSON), String.class);
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public Response deregister(String customerId) {
		return baseUrl.path("customer/deregister/" + customerId)
				.request()
				.delete();
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public Response getTokens(String customerId) {
		return baseUrl.path("customer/getTokens/" + customerId)
				.request()
				.get();
	}

	/**
	 * Creates tokens for a customer.
	 *
	 * @param tokenCreateRequest The request object containing the customer ID and token details.
	 * @return A Response indicating the success or failure of token creation.
	 * @author Jonas Puidokas (137282)
	 */
	public Response createTokens(TokenCreateRequest tokenCreateRequest) {
		return baseUrl.path("customer/createTokens")
				.request()
				.post(Entity.entity(tokenCreateRequest, MediaType.APPLICATION_JSON));
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public Response makePayment(PaymentRequest request) {
		return baseUrl.path("merchant/pay/")
				.request()
				.post(Entity.entity(request, MediaType.APPLICATION_JSON));
	}

}
