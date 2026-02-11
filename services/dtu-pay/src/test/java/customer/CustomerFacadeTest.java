package customer;

import customer.model.Customer;
import messaging.Event;
import messaging.MessageQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomerFacadeTest {

    private CustomerFacade facade;
    private MessageQueue mockQueue;
    private Map<String, Consumer<Event>> topicHandlers;
    private ArgumentCaptor<Event> eventCaptor;

    private static final Customer VALID_CUSTOMER = new Customer(
            "1234567890",
            "John",
            "Doe",
            "12345",
            "123 Main St"
    );

    /**
     * @author Jonas Puidokas (137282)
     */
    @BeforeEach
    void setUp() {
        mockQueue = mock(MessageQueue.class);
        eventCaptor = ArgumentCaptor.forClass(Event.class);
        topicHandlers = new HashMap<>();

        // Capture handlers during registration
        doAnswer(invocation -> {
            String topic = invocation.getArgument(0);
            Consumer<Event> handler = invocation.getArgument(1);
            topicHandlers.put(topic, handler);
            return null;
        }).when(mockQueue).addHandler(anyString(), any());

        facade = new CustomerFacade(mockQueue);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Test
    void constructor_ShouldRegisterAllEventHandlers() {
        verify(mockQueue, times(4)).addHandler(anyString(), any());
        assertEquals(4, topicHandlers.size());
        assertTrue(topicHandlers.containsKey("CustomerRegistered"));
        assertTrue(topicHandlers.containsKey("CustomerDeregistered"));
        assertTrue(topicHandlers.containsKey("CustomerTokensResponse"));
        assertTrue(topicHandlers.containsKey("XTokensRequest"));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Test
    void register_ValidCustomer_ShouldPublishEventAndReturnCustomerId() throws ExecutionException, InterruptedException {
        // Arrange
        String expectedCustomerId = "test-id";

        // Start the registration process
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            try {
                return facade.register(VALID_CUSTOMER);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Let the async operation run a bit
        Thread.sleep(100);

        // Verify and capture the published event
        verify(mockQueue).publish(eventCaptor.capture());
        Event publishedEvent = eventCaptor.getValue();
        assertEquals("CustomerRegistrationRequested", publishedEvent.getTopic());
        Customer publishedCustomer = publishedEvent.getArgument(0, Customer.class);
        assertEquals(VALID_CUSTOMER, publishedCustomer);

        // Simulate response
        Consumer<Event> handler = topicHandlers.get("CustomerRegistered");
        assertNotNull(handler, "Handler should exist for CustomerRegistered");
        handler.accept(new Event("CustomerRegistered", expectedCustomerId));

        // Assert
        String result = future.get();
        assertEquals(expectedCustomerId, result);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Test
    void deregister_ShouldPublishEventAndReturnResult() throws ExecutionException, InterruptedException {
        // Arrange
        UUID customerId = UUID.randomUUID();

        // Start the deregistration process
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                return facade.deregister(customerId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        // Let the async operation run a bit
        Thread.sleep(100);

        // Verify and capture the published event
        verify(mockQueue).publish(eventCaptor.capture());
        Event publishedEvent = eventCaptor.getValue();
        assertEquals("CustomerDeregistrationRequested", publishedEvent.getTopic());
        assertEquals(customerId, publishedEvent.getArgument(0, UUID.class));

        // Simulate response
        Consumer<Event> handler = topicHandlers.get("CustomerDeregistered");
        assertNotNull(handler, "Handler should exist for CustomerDeregistered");
        handler.accept(new Event("CustomerDeregistered", true));

        // Assert
        boolean result = future.get();
        assertTrue(result);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Disabled
    @Test
    void getTokens_ShouldPublishEventAndReturnTokenCount() throws ExecutionException, InterruptedException {
        // Arrange
        UUID customerId = UUID.randomUUID();
        int expectedTokens = 100;

        // Start the token retrieval process
//        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
//            try {
//                return facade.getTokens(customerId);
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        });

        // Let the async operation run a bit
        Thread.sleep(100);

        // Verify and capture the published event
        verify(mockQueue).publish(eventCaptor.capture());
        Event publishedEvent = eventCaptor.getValue();
        assertEquals("GetCustomerTokens", publishedEvent.getTopic());
        assertEquals(customerId, publishedEvent.getArgument(0, UUID.class));

        // Simulate response
        Consumer<Event> handler = topicHandlers.get("CustomerTokenReceived");
        assertNotNull(handler, "Handler should exist for CustomerTokenReceived");
        handler.accept(new Event("CustomerTokenReceived", expectedTokens));

        // Assert
//        int result = future.get();
//        assertEquals(expectedTokens, result);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @ParameterizedTest
    @MethodSource("provideInvalidCustomers")
    void register_InvalidCustomer_ShouldThrowException(Customer invalidCustomer, String expectedErrorMessage) {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> facade.register(invalidCustomer)
        );
        assertEquals(expectedErrorMessage, exception.getMessage());
        verify(mockQueue, never()).publish(any());
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    private static Stream<Arguments> provideInvalidCustomers() {
        return Stream.of(
                Arguments.of(
                        new Customer(null, "John", "Doe", "12345", "123 Main St"),
                        "CPR must be 10 digits"
                ),
                Arguments.of(
                        new Customer("123", "John", "Doe", "12345", "123 Main St"),
                        "CPR must be 10 digits"
                ),
                Arguments.of(
                        new Customer("1234567890", "", "Doe", "12345", "123 Main St"),
                        "First name cannot be blank"
                ),
                Arguments.of(
                        new Customer("1234567890", "John", "", "12345", "123 Main St"),
                        "Last name cannot be blank"
                ),
                Arguments.of(
                        new Customer("1234567890", "John", "Doe", "", "123 Main St"),
                        "Account cannot be blank"
                ),
                Arguments.of(
                        new Customer("1234567890", "John", "Doe", "12345", ""),
                        "Address cannot be blank"
                )
        );
    }
}