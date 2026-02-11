package customer;

import customer.model.Customer;
import customer.model.TokenCreateRequest;
import jakarta.inject.Singleton;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Singleton
public class CustomerFacade {
	
	CompletableFuture<String> customerId;
	CompletableFuture<List<UUID>> tokens;
	CompletableFuture<Boolean> deregistered;
	CompletableFuture<Boolean> tokensCreated;

	MessageQueue queue;
	
	public CustomerFacade() {
		this(new RabbitMqQueue("rabbitmq"));
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public CustomerFacade(MessageQueue q) {
		queue = q;
		q.addHandler("CustomerRegistered", this::policyCustomerRegistered);
		q.addHandler("CustomerDeregistered", this::policyCustomerDeregistered);
		q.addHandler("CustomerTokensResponse", this::policyCustomerTokenReceived);
		q.addHandler("XTokensRequest", this::policyCustomerTokensCreated);
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
    void policyCustomerRegistered(Event e) {
		customerId.complete(e.getArgument(0, String.class));
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
    void policyCustomerDeregistered(Event e) {
		deregistered.complete(e.getArgument(0, Boolean.class));
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
    void policyCustomerTokenReceived(Event e) {
		tokens.complete(e.getArgument(0, List.class));
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
    void policyCustomerTokensCreated(Event e) {
		tokensCreated.complete(e.getArgument(0, Boolean.class));
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public String register(Customer customer) throws InterruptedException, ExecutionException {
		// Validate customer account data before sending it further
		customer.validate();

		// Validation ok, sending data to account-manager
		customerId = new CompletableFuture<>();
		queue.publish(new Event("CustomerRegistrationRequested", customer));
		return customerId.get();
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public boolean deregister(UUID customerId) throws ExecutionException, InterruptedException {
		deregistered = new CompletableFuture<>();
		queue.publish(new Event("CustomerDeregistrationRequested", customerId));
		return deregistered.get();
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public boolean createTokens(TokenCreateRequest tokenCreateRequest) throws InterruptedException, ExecutionException {
		tokensCreated = new CompletableFuture<>();
		queue.publish(new Event("GenerateTokensForCustomer", tokenCreateRequest));
		return tokensCreated.get();
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public List<UUID> getTokens(UUID customerId) throws InterruptedException, ExecutionException {
		tokens = new CompletableFuture<>();
		queue.publish(new Event("GetCustomerTokens", customerId));
		return tokens.get();
	}

	/**
	 * @author Jonas Puidokas (137282)
	 */
	public List<Object> getCustomerTransactions(UUID customerId) throws InterruptedException, ExecutionException {
		CompletableFuture<List<Object>> transactionsFuture = new CompletableFuture<>();

		queue.addHandler("CustomerTransactions", e -> {
			transactionsFuture.complete(e.getArgument(0, List.class));
		});

		queue.publish(new Event("GetCustomerTransactions", customerId));
		return transactionsFuture.get();
	}
}
