package merchant.unit;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import merchant.Merchant;
import merchant.MerchantFacade;
import merchant.PaymentResponse;
import messaging.Event;
import messaging.MessageQueue;

class MerchantFacadeTest {

    private MerchantFacade facade;
    private MessageQueue queue;

    @BeforeEach
    void setup() {
        queue = mock(MessageQueue.class);

        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            @SuppressWarnings("unchecked")
            var handler = (java.util.function.Consumer<Event>) invocation.getArgument(1);

            if ("MerchantRegistered".equals(topic)) {
                handler.accept(new Event("MerchantRegistered", "mocked-merchant-id"));
            } else if ("MerchantValidationResponse".equals(topic)) {
                handler.accept(new Event("MerchantValidationResponse", true));
            } else if ("MerchantAccountResponse".equals(topic)) {
                handler.accept(new Event("MerchantAccountResponse", "mocked-merchant-account"));
            } else if ("PaymentSucceeded".equals(topic)) {
                handler.accept(new Event("PaymentSucceeded"));
            } else if ("PaymentFailed".equals(topic)) {
                handler.accept(new Event("PaymentFailed"));
            }

            return null;
        }).when(queue).addHandler(anyString(), any());

        facade = new MerchantFacade(queue);
    }

    @Test
    void testRegisterMerchant() throws ExecutionException, InterruptedException {
        // Arrange
        Merchant merchant = new Merchant("1234567890", "John", "Doe", "mocked-account", "mocked-address");

        // Act
        String result = facade.register(merchant);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals("MerchantRegistrationRequested", capturedEvent.getTopic());
        assertNotNull(result);
        assertEquals("mocked-merchant-id", result);
    }

    @Test
    void testDeregisterMerchant() throws InterruptedException, ExecutionException {
        // Arrange
        UUID merchantId = UUID.randomUUID();

        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            if ("MerchantDeregistered".equals(topic)) {
                Event event = new Event("MerchantDeregistered", new Object[]{true});
                @SuppressWarnings("unchecked")
                var handler = (java.util.function.Consumer<Event>) invocation.getArgument(1);
                handler.accept(event);
            }
            return null;
        }).when(queue).addHandler(eq("MerchantDeregistered"), any());

        // Act
        boolean result = facade.deregister(merchantId);

        // Assert
        assertTrue(result, "Merchant deregistration should succeed");
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals("MerchantDeregistrationRequested", capturedEvent.getTopic());
        assertEquals(merchantId, capturedEvent.getArgument(0, UUID.class));
    }

    @Test
    void testInitiatePayment_Success() throws Exception {
        // Arrange
        UUID token = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        double amount = 100.0;
        String description = "Test Payment";
        String merchantAccount = "test-account";

        doAnswer(invocation -> {
            Event event = invocation.getArgument(0, Event.class);
            if ("ValidateMerchant".equals(event.getTopic())) {
                queue.addHandler("MerchantValidationResponse", e -> {
                    boolean isValid = true;
                    e.getArgument(0, Boolean.class);
                });
            } else if ("GetMerchantAccount".equals(event.getTopic())) {
                queue.addHandler("MerchantAccountResponse", e -> {
                    e.getArgument(0, String.class);
                });
            }
            return null;
        }).when(queue).publish(any(Event.class));

        doAnswer(invocation -> {
            Event event = invocation.getArgument(0, Event.class);
            if ("PaymentRequested".equals(event.getTopic())) {
                facade.handlePaymentSucceeded(new Event("PaymentSucceeded"));
            }
            return null;
        }).when(queue).publish(any(Event.class));

        // Act
        PaymentResponse response = facade.initiatePayment(token, merchantId, amount, description);

        // Assert
        assertNotNull(response, "PaymentResponse should not be null");
        assertTrue(response.isSuccess(), "Payment should succeed");
        assertEquals("Payment succeeded", response.getMessage(), "Success message should match");
        assertNotNull(response.getTransactionId(), "Transaction ID should not be null");
    }

    @Test
    void testInitiatePayment_Failure() throws ExecutionException, InterruptedException {
        // Arrange
        UUID token = UUID.randomUUID();
        UUID merchantId = UUID.randomUUID();
        double amount = 100.0;
        String description = "Test payment";

        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            if ("ValidateMerchant".equals(topic)) {
                queue.publish(new Event("MerchantValidationResponse", false));
            }
            return null;
        }).when(queue).publish(any(Event.class));

        // Act
        PaymentResponse response = facade.initiatePayment(token, merchantId, amount, description);

        // Assert
        assertFalse(response.isSuccess(), "Payment should fail");
        assertEquals("Invalid merchant ID", response.getMessage(), "Failure message should match");
        assertNull(response.getTransactionId(), "Transaction ID should be null for a failed payment");
    }

    @Test
    void testHandlePaymentSucceeded() {
        // Arrange
        Event event = new Event("PaymentSucceeded");
        facade.paymentResult = new CompletableFuture<>(); 

        // Act
        facade.handlePaymentSucceeded(event);

        // Assert
        assertTrue(facade.paymentResult.isDone(), "Payment result should be completed");
        assertTrue(facade.paymentResult.join(), "Payment result should be true");
    }

    @Test
    void testHandlePaymentFailed() {
        // Arrange
        Event event = new Event("PaymentFailed");
        facade.paymentResult = new CompletableFuture<>(); 

        // Act
        facade.handlePaymentFailed(event);

        // Assert
        assertTrue(facade.paymentResult.isDone(), "Payment result should be completed");
        assertFalse(facade.paymentResult.join(), "Payment result should be false");
    }

    @Test
    void testValidateMerchant() throws ExecutionException, InterruptedException {
        // Arrange
        UUID merchantId = UUID.randomUUID();

        // Act
        boolean result = facade.validateMerchant(merchantId);

        // Assert
        assertTrue(result);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals("ValidateMerchant", capturedEvent.getTopic());
        assertEquals(merchantId, capturedEvent.getArgument(0, UUID.class));
    }

    @Test
    void testGetMerchantAccount() throws ExecutionException, InterruptedException {
        // Arrange
        UUID merchantId = UUID.randomUUID();

        // Act
        String result = facade.getMerchantAccount(merchantId);

        // Assert
        assertEquals("mocked-merchant-account", result);
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals("GetMerchantAccount", capturedEvent.getTopic());
        assertEquals(merchantId, capturedEvent.getArgument(0, UUID.class));
    }
    
}
