package unit;

import messaging.Event;
import messaging.MessageQueue;
import model.PaymentRequest;
import services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

/**
 * @author Asher Sharif (240193)
 */
class PaymentServiceTest {

    private PaymentService paymentService;
    private MessageQueue mockQueue;

    @BeforeEach
    void setup() {
        mockQueue = mock(MessageQueue.class);
        paymentService = spy(new PaymentService(mockQueue)); // Use spy for partial mocking
    }

    @Test
    void testPolicyPaymentRequested_Success() {
        // Arrange
        UUID token = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        Event event = new Event("PaymentRequested", token, merchantId, "merchant-account", 100.0, "description");

        doReturn("customer123").when(paymentService).validateTokenAndGetCustomerId(token);
        doReturn("customer-account").when(paymentService).retrieveCustomerAccount("customer123");
        doReturn(true).when(paymentService).transferMoney(anyString(), anyString(), anyDouble(), anyString());

        // Act
        paymentService.policyPaymentRequested(event);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue, times(2)).publish(eventCaptor.capture()); // PaymentReported and PaymentSucceeded
        Event successEvent = eventCaptor.getAllValues().get(1);
        assertEquals("PaymentSucceeded", successEvent.getTopic());
    }

    @Test
    void testPolicyPaymentRequested_InvalidToken() {
        // Arrange
        UUID token = UUID.randomUUID();
        Event event = new Event("PaymentRequested", token, UUID.randomUUID(), "merchant-account", 100.0, "description");

        doReturn(null).when(paymentService).validateTokenAndGetCustomerId(token);

        // Act
        paymentService.policyPaymentRequested(event);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());
        Event failureEvent = eventCaptor.getValue();
        assertEquals("PaymentFailed", failureEvent.getTopic());
    }

    @Test
    void testProcessPayment_Success() {
        // Arrange
        PaymentRequest request = new PaymentRequest(UUID.randomUUID(), UUID.randomUUID(),"randomId", "customer-account", "merchant-account", 100.0, "description");
        doReturn(true).when(paymentService).transferMoney(anyString(), anyString(), anyDouble(), anyString());

        // Act
        paymentService.processPayment(request);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue, times(2)).publish(eventCaptor.capture()); // PaymentReported and PaymentSucceeded
        Event successEvent = eventCaptor.getAllValues().get(1);
        assertEquals("PaymentSucceeded", successEvent.getTopic());
    }

    @Test
    void testProcessPayment_Failure() {
        // Arrange
        PaymentRequest request = new PaymentRequest(UUID.randomUUID(), UUID.randomUUID(),"randomId", "customer-account", "merchant-account", 100.0, "description");
        doReturn(false).when(paymentService).transferMoney(anyString(), anyString(), anyDouble(), anyString());

        // Act
        paymentService.processPayment(request);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue, times(2)).publish(eventCaptor.capture()); // PaymentReported and PaymentFailed
        Event failureEvent = eventCaptor.getAllValues().get(1);
        assertEquals("PaymentFailed", failureEvent.getTopic());
    }

    @Test
    void testValidateTokenAndGetCustomerId_Success() throws Exception {
        // Arrange
        UUID token = UUID.randomUUID();
        String customerId = "customer123";

        Map<String, Object> responsePayload = new HashMap<>();
        responsePayload.put("token", token);
        responsePayload.put("customerId", customerId);
        responsePayload.put("message", "Token validated successfully.");

        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Event> handler = invocation.getArgument(1, java.util.function.Consumer.class);

            if ("ValidateCustomerTokenResponse".equals(topic)) {
                handler.accept(new Event("ValidateCustomerTokenResponse", new Object[]{responsePayload}));
            }
            return null;
        }).when(mockQueue).addHandler(eq("ValidateCustomerTokenResponse"), any());

        // Act
        String result = paymentService.validateTokenAndGetCustomerId(token);

        // Assert
        assertEquals(customerId, result, "Customer ID should match the mock response.");
    }


    @Test
    void testValidateTokenAndGetCustomerId_Failure() throws Exception {
        // Arrange
        UUID token = UUID.randomUUID();

        Map<String, Object> responsePayload = new HashMap<>();
        responsePayload.put("token", token);
        responsePayload.put("customerId", null);
        responsePayload.put("message", "Token not valid or does not exist.");

        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Event> handler = invocation.getArgument(1, java.util.function.Consumer.class);

            if ("ValidateCustomerTokenResponse".equals(topic)) {
                handler.accept(new Event("ValidateCustomerTokenResponse", new Object[]{responsePayload}));
            }
            return null;
        }).when(mockQueue).addHandler(eq("ValidateCustomerTokenResponse"), any());

        // Act
        String result = paymentService.validateTokenAndGetCustomerId(token);

        // Assert
        assertNull(result, "Customer ID should be null when the token is invalid.");
    }


    @Test
    void testRetrieveCustomerAccount_Success() throws Exception {
        // Arrange
        String customerId = "customer123";
        String customerAccount = "customer-account";

        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Event> handler = invocation.getArgument(1, java.util.function.Consumer.class);

            if ("CustomerAccountResponse".equals(topic)) {
                handler.accept(new Event("CustomerAccountResponse", customerAccount));
            }
            return null;
        }).when(mockQueue).addHandler(eq("CustomerAccountResponse"), any());

        // Act
        String result = paymentService.retrieveCustomerAccount(customerId);

        // Assert
        assertEquals(customerAccount, result, "Customer account should match the mock response.");
    }

    @Test
    void testRetrieveCustomerAccount_Failure() throws Exception {
        // Arrange
        String customerId = "customer123";

        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            @SuppressWarnings("unchecked")
            java.util.function.Consumer<Event> handler = invocation.getArgument(1, java.util.function.Consumer.class);

            if ("CustomerAccountResponse".equals(topic)) {
                handler.accept(new Event("CustomerAccountResponse", (Object) null));
            }
            return null;
        }).when(mockQueue).addHandler(eq("CustomerAccountResponse"), any());

        // Act
        String result = paymentService.retrieveCustomerAccount(customerId);

        // Assert
        assertNull(result, "Customer account should be null when the account does not exist.");
    }

}
