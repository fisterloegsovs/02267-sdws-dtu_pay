package services;

import messaging.Event;
import messaging.MessageQueue;
import modal.Transaction;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ReportingService {

    private final MessageQueue queue;
    private final Map<UUID, Transaction> transactions = new HashMap<>();
    private static final Logger logger = Logger.getLogger(ReportingService.class.getName());

    public ReportingService(MessageQueue queue) {
        this.queue = queue;
        logger.info("Initializing ReportingService...");

        // Register event handlers
        queue.addHandler("PaymentReported", this::policyPaymentReported);
        queue.addHandler("GetMerchantTransactions", this::policyGetMerchantTransactions);
        queue.addHandler("GetCustomerTransactions", this::policyGetCustomerTransactions);
        queue.addHandler("GetAllTransactions", this::policyGetAllTransactions);

        logger.info("Event handlers registered.");
    }

    public void policyPaymentReported(Event event) {
        UUID transactionId = event.getArgument(0, UUID.class);
        UUID token = event.getArgument(1, UUID.class);
        UUID merchantId = event.getArgument(2, UUID.class);
        String customerId = event.getArgument(3, String.class);
        String customerAccount = event.getArgument(4, String.class);
        String merchantAccount = event.getArgument(5, String.class);
        double amount = event.getArgument(6, Double.class);
        String description = event.getArgument(7, String.class);
        String status = event.getArgument(8, String.class);

        
        Transaction transaction = new Transaction(transactionId, token, merchantId, customerId, customerAccount, merchantAccount, amount, description, status);
        transactions.put(transactionId, transaction);

        
        logger.info("Transaction reported and stored: " + transaction);
    }

    public void policyGetMerchantTransactions(Event event) {
        UUID merchantId = event.getArgument(0, UUID.class);

        List<Map<String, Object>> filteredTransactions = transactions.values().stream()
                .filter(t -> t.getMerchantId().equals(merchantId))
                .map(t -> {
                    Map<String, Object> transactionData = new HashMap<>();
                    transactionData.put("transactionId", t.getTransactionId());
                    transactionData.put("token", t.getToken());
                    transactionData.put("merchantId", t.getMerchantId());
                    transactionData.put("merchantAccount", t.getMerchantAccount());
                    transactionData.put("amount", t.getAmount());
                    transactionData.put("description", t.getDescription());
                    transactionData.put("status", t.getStatus());
                    return transactionData;
                })
                .collect(Collectors.toList());
        logger.info("The merchant transactions are:"+filteredTransactions);
        
        Event response = new Event("MerchantTransactions", new Object[]{filteredTransactions});
        queue.publish(response);
        logger.info("event pushed");
    }

    public void policyGetCustomerTransactions(Event event) {
        String customerId = event.getArgument(0, String.class);

        List<Map<String, Object>> filteredTransactions = transactions.values().stream()
                .filter(t -> t.getCustomerId().equals(customerId))
                .map(t -> {
                    Map<String, Object> transactionData = new HashMap<>();
                    transactionData.put("transactionId", t.getTransactionId());
                    transactionData.put("token", t.getToken());
                    transactionData.put("customerId", t.getCustomerId());
                    transactionData.put("customerAccount", t.getCustomerAccount());
                    transactionData.put("amount", t.getAmount());
                    transactionData.put("description", t.getDescription());
                    transactionData.put("status", t.getStatus());
                    return transactionData;
                })
                .collect(Collectors.toList());

        
        Event response = new Event("CustomerTransactions", new Object[]{filteredTransactions});
        queue.publish(response);
    }

    public void policyGetAllTransactions(Event event) {
        List<Transaction> allTransactions = new ArrayList<>(transactions.values());

        
        Event response = new Event("AllTransactions", new Object[]{allTransactions});
        queue.publish(response);
    }

}
