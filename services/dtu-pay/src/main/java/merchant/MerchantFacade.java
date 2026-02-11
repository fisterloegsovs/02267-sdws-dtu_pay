package merchant;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import jakarta.inject.Singleton;
import messaging.Event;
import messaging.MessageQueue;
import messaging.implementations.RabbitMqQueue;

	/**
	 * @author Asher Sharif (240193)
	 */
@Singleton
public class MerchantFacade {

	private final MessageQueue queue;
	public CompletableFuture<Boolean> paymentResult;
	private static final Logger logger = Logger.getLogger(MerchantFacade.class.getName());

	public MerchantFacade() {
		this(new RabbitMqQueue("rabbitmq"));
	}

	public MerchantFacade(MessageQueue queue) {
		this.queue = queue;

		/* Payment */
		queue.addHandler("PaymentSucceeded", this::handlePaymentSucceeded);
		queue.addHandler("PaymentFailed", this::handlePaymentFailed);
	}


	public String register(Merchant merchant) throws InterruptedException, ExecutionException {
		CompletableFuture<String> merchantIdFuture = new CompletableFuture<>();
		queue.addHandler("MerchantRegistered", e -> {
			merchantIdFuture.complete(e.getArgument(0, String.class));
		});
		queue.publish(new Event("MerchantRegistrationRequested", new Object[]{merchant}));
		return merchantIdFuture.get();
	}

	public boolean deregister(UUID merchantId) {
		CompletableFuture<Boolean> deregistrationResponse = new CompletableFuture<>();

		queue.addHandler("MerchantDeregistered", e -> {
			Boolean result = e.getArgument(0, Boolean.class);
			deregistrationResponse.complete(result);
		});
		queue.publish(new Event("MerchantDeregistrationRequested", new Object[]{merchantId}));

		try {
			return deregistrationResponse.get();
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException("Error during merchant deregistration", e);
		}
	}

	/* Initiate Payment */
	public PaymentResponse initiatePayment(UUID token, UUID merchantId, double amount, String description)
			throws InterruptedException, ExecutionException {
		logger.info("Initiating payment");

		if (!validateMerchant(merchantId)) {
			logger.warning("Merchant validation failed for ID: " + merchantId);
			return new PaymentResponse(false, "Invalid merchant ID", null);
		}

		String merchantAccount = getMerchantAccount(merchantId);
		if (merchantAccount == null) {
			logger.warning("Failed to retrieve merchant account for ID: " + merchantId);
			return new PaymentResponse(false, "Merchant account not found", null);
		}

		paymentResult = new CompletableFuture<>();
		queue.publish(new Event("PaymentRequested", new Object[]{
				token, merchantId, merchantAccount, amount, description
		}));

		boolean success = paymentResult.get();
		UUID transactionId = success ? UUID.randomUUID() : null;

		String message = success ? "Payment succeeded" : "Payment failed. Check the details for more information.";
		return new PaymentResponse(success, message, transactionId);
	}


	public void handlePaymentSucceeded(Event event) {
		if (paymentResult != null) {
			paymentResult.complete(true);
		}
	}

	public void handlePaymentFailed(Event event) {
		if (paymentResult != null) {
			paymentResult.complete(false);
		}
	}


	public boolean validateMerchant(UUID merchantId) {
		try {
			CompletableFuture<Boolean> validationResult = new CompletableFuture<>();
			queue.addHandler("MerchantValidationResponse", e -> {
				boolean exists = e.getArgument(0, Boolean.class);
				validationResult.complete(exists);
			});

			queue.publish(new Event("ValidateMerchant", new Object[]{merchantId}));
			return validationResult.get();
		} catch (Exception e) {
			logger.severe("Error validating merchant: " + e.getMessage());
			return false;
		}
	}


	public String getMerchantAccount(UUID merchantId) {
		try {
			CompletableFuture<String> accountFuture = new CompletableFuture<>();
			queue.addHandler("MerchantAccountResponse", e -> {
				String account = e.getArgument(0, String.class);
				accountFuture.complete(account);
			});

			queue.publish(new Event("GetMerchantAccount", new Object[]{merchantId}));
			return accountFuture.get();
		} catch (Exception e) {
			logger.severe("Error retrieving merchant account: " + e.getMessage());
			return null;
		}
	}

	public List<Object> getMerchantTransactions(UUID merchantId) throws InterruptedException, ExecutionException {
		CompletableFuture<List<Object>> transactionsFuture = new CompletableFuture<>();

		queue.addHandler("MerchantTransactions", e -> {
			transactionsFuture.complete(e.getArgument(0, List.class));
		});

		queue.publish(new Event("GetMerchantTransactions", merchantId));
		return transactionsFuture.get();
	}

}

