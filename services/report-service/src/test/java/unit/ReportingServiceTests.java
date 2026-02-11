package unit;

import messaging.Event;
import messaging.MessageQueue;
import modal.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import services.ReportingService;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportingServiceTest {

	private ReportingService reportingService;
	private MessageQueue mockQueue;

	@BeforeEach
	void setup() {
		mockQueue = mock(MessageQueue.class);
		reportingService = new ReportingService(mockQueue);
	}

	@Test
	void testPolicyPaymentReported() {
		// Arrange
		UUID transactionId = UUID.randomUUID();
		UUID token = UUID.randomUUID();
		UUID merchantId = UUID.randomUUID();
		String customerId = "customer-123";
		String customerAccount = "customer-account";
		String merchantAccount = "merchant-account";
		double amount = 150.75;
		String description = "Test Transaction";
		String status = "success";

		Event event = new Event("PaymentReported", transactionId, token, merchantId, customerId, customerAccount, merchantAccount, amount, description, status);

		// Act
		reportingService.policyPaymentReported(event);

		// Assert
		ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(mockQueue, never()).publish(eventCaptor.capture()); // No events are published in this policy
	}

	@Test
	void testPolicyGetMerchantTransactions() {
		// Arrange
		UUID transactionId1 = UUID.randomUUID();
		UUID transactionId2 = UUID.randomUUID();
		UUID merchantId = UUID.randomUUID();
		Transaction transaction1 = new Transaction(transactionId1, UUID.randomUUID(), merchantId, "customer-1", "customer-account-1", "merchant-account", 100.0, "Test 1", "success");
		Transaction transaction2 = new Transaction(transactionId2, UUID.randomUUID(), merchantId, "customer-2", "customer-account-2", "merchant-account", 200.0, "Test 2", "success");
		reportingService.policyPaymentReported(new Event("PaymentReported", transaction1.getTransactionId(), transaction1.getToken(), transaction1.getMerchantId(), transaction1.getCustomerId(), transaction1.getCustomerAccount(), transaction1.getMerchantAccount(), transaction1.getAmount(), transaction1.getDescription(), transaction1.getStatus()));
		reportingService.policyPaymentReported(new Event("PaymentReported", transaction2.getTransactionId(), transaction2.getToken(), transaction2.getMerchantId(), transaction2.getCustomerId(), transaction2.getCustomerAccount(), transaction2.getMerchantAccount(), transaction2.getAmount(), transaction2.getDescription(), transaction2.getStatus()));

		Event event = new Event("GetMerchantTransactions", merchantId);

		// Act
		reportingService.policyGetMerchantTransactions(event);

		// Assert
		ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(mockQueue, times(1)).publish(eventCaptor.capture());

		Event capturedEvent = eventCaptor.getValue();
		assertEquals("MerchantTransactions", capturedEvent.getTopic());
		List<?> transactions = capturedEvent.getArgument(0, List.class);
		assertEquals(2, transactions.size(), "Should return all transactions for the merchant");
	}

	@Test
	void testPolicyGetCustomerTransactions() {
		// Arrange
		UUID transactionId = UUID.randomUUID();
		UUID merchantId = UUID.randomUUID();
		String customerId = "customer-123";
		Transaction transaction = new Transaction(transactionId, UUID.randomUUID(), merchantId, customerId, "customer-account", "merchant-account", 150.0, "Test Transaction", "success");
		reportingService.policyPaymentReported(new Event("PaymentReported", transaction.getTransactionId(), transaction.getToken(), transaction.getMerchantId(), transaction.getCustomerId(), transaction.getCustomerAccount(), transaction.getMerchantAccount(), transaction.getAmount(), transaction.getDescription(), transaction.getStatus()));

		Event event = new Event("GetCustomerTransactions", customerId);

		// Act
		reportingService.policyGetCustomerTransactions(event);

		// Assert
		ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(mockQueue, times(1)).publish(eventCaptor.capture());

		Event capturedEvent = eventCaptor.getValue();
		assertEquals("CustomerTransactions", capturedEvent.getTopic());
		List<?> transactions = capturedEvent.getArgument(0, List.class);
		assertEquals(1, transactions.size(), "Should return all transactions for the customer");
	}

	@Test
	void testPolicyGetAllTransactions() {
		// Arrange
		UUID transactionId1 = UUID.randomUUID();
		UUID transactionId2 = UUID.randomUUID();
		Transaction transaction1 = new Transaction(transactionId1, UUID.randomUUID(), UUID.randomUUID(), "customer-1", "customer-account-1", "merchant-account", 100.0, "Test 1", "success");
		Transaction transaction2 = new Transaction(transactionId2, UUID.randomUUID(), UUID.randomUUID(), "customer-2", "customer-account-2", "merchant-account", 200.0, "Test 2", "success");
		reportingService.policyPaymentReported(new Event("PaymentReported", transaction1.getTransactionId(), transaction1.getToken(), transaction1.getMerchantId(), transaction1.getCustomerId(), transaction1.getCustomerAccount(), transaction1.getMerchantAccount(), transaction1.getAmount(), transaction1.getDescription(), transaction1.getStatus()));
		reportingService.policyPaymentReported(new Event("PaymentReported", transaction2.getTransactionId(), transaction2.getToken(), transaction2.getMerchantId(), transaction2.getCustomerId(), transaction2.getCustomerAccount(), transaction2.getMerchantAccount(), transaction2.getAmount(), transaction2.getDescription(), transaction2.getStatus()));

		Event event = new Event("GetAllTransactions", new Object[]{});

		// Act
		reportingService.policyGetAllTransactions(event);

		// Assert
		ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
		verify(mockQueue, times(1)).publish(eventCaptor.capture());

		Event capturedEvent = eventCaptor.getValue();
		assertEquals("AllTransactions", capturedEvent.getTopic());
		List<?> transactions = capturedEvent.getArgument(0, List.class);
		assertEquals(2, transactions.size(), "Should return all transactions");
	}
}