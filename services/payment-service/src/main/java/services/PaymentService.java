package services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import messaging.Event;
import messaging.MessageQueue;
import model.PaymentRequest;
import model.PaymentTransaction;

/**
 * @author Asher Sharif (240193)
 */
public class PaymentService {

    private final MessageQueue queue;
    private final Map<UUID, PaymentTransaction> transactions = new HashMap<>();
    private static final Logger logger = Logger.getLogger(PaymentService.class.getName());

    public PaymentService(MessageQueue queue) {
        this.queue = queue;
        logger.info("Initializing PaymentService...");
        queue.addHandler("PaymentRequested", this::policyPaymentRequested);
        logger.info("Event handler for 'PaymentRequested' registered.");
    }

    public void policyPaymentRequested(Event event) {
        logger.info("Received 'PaymentRequested' event: " + event);

        UUID token = event.getArgument(0, UUID.class);
        UUID merchantId = event.getArgument(1, UUID.class);
        String merchantAccount = event.getArgument(2, String.class);
        double amount = event.getArgument(3, Double.class);
        String description = event.getArgument(4, String.class);

        logger.info("Validating token and retrieving customer ID...");
        String customerId = validateTokenAndGetCustomerId(token);

        if (customerId == null) {
            logger.warning("Token validation failed. Publishing 'PaymentFailed' event.");
            queue.publish(new Event("PaymentFailed", new Object[]{token, "Invalid or expired token"}));
            return;
        }

        logger.info("Retrieving customer account for customer ID: " + customerId);
        String customerAccount = retrieveCustomerAccount(customerId);

        if (customerAccount == null) {
            logger.warning("Failed to retrieve customer account. Publishing 'PaymentFailed' event.");
            queue.publish(new Event("PaymentFailed", new Object[]{token, "Customer account not found"}));
            return;
        }

        logger.info("Proceeding to process payment...");
        processPayment(new PaymentRequest(token, merchantId, customerId  ,customerAccount, merchantAccount, amount, description));
    }

    public void processPayment(PaymentRequest request) {
        logger.info("Initiating bank transfer...");
        UUID transactionId = UUID.randomUUID();
        String status;
        String description;

        try {
            boolean success = transferMoney(
                    request.customerAccount(),
                    request.merchantAccount(),
                    request.amount(),
                    request.description()
            );

            if (success) {
                logger.info("Payment successful. Transaction ID: " + transactionId);
                transactions.put(transactionId, new PaymentTransaction(
                        request.customerAccount(),
                        request.merchantAccount(),
                        request.amount(),
                        request.description()
                ));
                status = "success";
                description = request.description();
            } else {
                logger.warning("Bank transfer failed without exception for request: " + request);
                status = "failure";
                description = "Bank transfer failed without exception.";
            }
        } catch (Exception e) {
            logger.severe("Bank transfer failed with exception: " + e.getMessage());
            status = "failure";
            description = "Exception: " + e.getMessage();
        }

        logger.info("Publishing 'PaymentReported' event for transaction ID: " + transactionId);
        queue.publish(new Event("PaymentReported", new Object[]{
                transactionId,
                request.token(),
                request.merchantId(),
                request.customerId(),
                request.customerAccount(),
                request.merchantAccount(),
                request.amount(),
                description,
                status
        }));

        if (status.equals("success")) {
            queue.publish(new Event("PaymentSucceeded", new Object[]{transactionId}));
        } else {
            queue.publish(new Event("PaymentFailed", new Object[]{request.token(), description}));
        }
    }

    public String validateTokenAndGetCustomerId(UUID token) {
        try {
            CompletableFuture<String> validationFuture = new CompletableFuture<>();
            queue.addHandler("ValidateCustomerTokenResponse", event -> {
                logger.info("Validation response received: " + event);
                Map<String, Object> responsePayload = event.getArgument(0, Map.class);
                if (responsePayload != null) {
                    String customerIdStr = responsePayload.get("customerId").toString();
                    String message = responsePayload.get("message").toString();
                    if (customerIdStr != null) {
                        logger.info("Token is valid. Associated customer ID: " + customerIdStr);
                        validationFuture.complete(customerIdStr);
                    } else {
                        logger.warning("Token is invalid. Error message: " + message);
                        validationFuture.completeExceptionally(new IllegalArgumentException(message));
                    }
                } else {
                    logger.warning("Response payload is null.");
                    validationFuture.completeExceptionally(new IllegalArgumentException("Invalid response payload."));
                }
            });
            logger.info("Publishing event: ValidateTokenRequest with token: " + token);
            queue.publish(new Event("ValidateCustomerTokenRequest", new Object[]{token}));
            return validationFuture.get();
        } catch (Exception e) {
            logger.severe("Token validation failed: " + e.getMessage());
            return null;
        }
    }

    public boolean transferMoney(String fromAccount, String toAccount, double amount, String description) {
        BankService bank = getBankService();
        try {
            bank.transferMoneyFromTo(fromAccount, toAccount, BigDecimal.valueOf(amount), description);
            logger.info("Bank transfer successful: " + amount + " from " + fromAccount + " to " + toAccount);
            return true;
        } catch (BankServiceException_Exception e) {
            logger.severe("Bank transfer failed: " + e.getMessage());
            if (e.getFaultInfo() != null) {
                logger.severe("Error details: " + e.getFaultInfo().getErrorMessage());
            }
            return false;
        }
    }

    public BankService getBankService() {
        return new BankServiceService().getBankServicePort();
    }

    public String retrieveCustomerAccount(String customerId) {
        try {
            logger.info("retriving customer account with:"+customerId);
            CompletableFuture<String> accountFuture = new CompletableFuture<>();

            queue.addHandler("CustomerAccountResponse", e -> {
                String account = e.getArgument(0, String.class);
                logger.info("got account:"+account);
                accountFuture.complete(account);
            });

            queue.publish(new Event("GetCustomerAccount", new Object[]{customerId}));
            return accountFuture.get();
        } catch (Exception e) {
            logger.severe("Error retrieving customer account: " + e.getMessage());
            return null;
        }
    }
}
