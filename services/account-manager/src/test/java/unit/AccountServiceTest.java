package unit;

import messaging.Event;
import messaging.MessageQueue;
import model.AccountService;
import model.Customer;
import model.Merchant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    private AccountService accountService;
    private MessageQueue mockQueue;

    @BeforeEach
    void setUp() {
        mockQueue = mock(MessageQueue.class);
        accountService = new AccountService(mockQueue);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Disabled
    @Test
    void createAccount_ShouldAddCustomerAndPublishEvent() {
        // Arrange
        Customer customer = new Customer("1234567890", "John", "Doe", "account1", "address1");

        // Act
        accountService.createAccount(customer);

        // Assert
        assertEquals(1, accountService.customers.size());
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("CustomerRegistered", publishedEvent.getTopic());
        assertNotNull(publishedEvent.getArgument(0, UUID.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Test
    void deregisterCustomer_ShouldRemoveCustomerAndPublishEvent() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("1234567890", "John", "Doe", "account1", "address1");
        accountService.customers.put(customerId, customer);

        // Act
        accountService.deregisterCustomer(customerId);

        // Assert
        assertFalse(accountService.customers.containsKey(customerId));
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("CustomerDeregistered", publishedEvent.getTopic());
        assertTrue(publishedEvent.getArgument(0, Boolean.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Test
    void deregisterCustomer_ShouldPublishEventForNonExistentCustomer() {
        // Arrange
        UUID customerId = UUID.randomUUID();

        // Act
        accountService.deregisterCustomer(customerId);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("CustomerDeregistered", publishedEvent.getTopic());
        assertFalse(publishedEvent.getArgument(0, Boolean.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    // TODO
    @Disabled
    @Test
    void getTokens_ShouldReturnCorrectTokenCount() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("1234567890", "John", "Doe", "account1", "address1");
        accountService.customers.put(customerId, customer);

        // Act
        accountService.getTokens(customerId);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("CustomerTokenReceived", publishedEvent.getTopic());
        assertEquals(1, publishedEvent.getArgument(0, Integer.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    // TODO
    @Disabled
    @Test
    void getTokens_ShouldPublishEventForNonExistentCustomer() {
        // Arrange
        UUID customerId = UUID.randomUUID();

        // Act
        accountService.getTokens(customerId);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("CustomerTokenReceived", publishedEvent.getTopic());
        assertNull(publishedEvent.getArgument(0, Integer.class));
    }

    @Test
    void createMerchantAccount_ShouldAddMerchantAndPublishEvent() {
        // Arrange
        Merchant merchant = new Merchant("1234567890", "Alice", "Smith", "merchant-account", "merchant-address");

        // Act
        accountService.createMerchantAccount(merchant);

        // Assert
        assertEquals(1, accountService.merchants.size());
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("MerchantRegistered", publishedEvent.getTopic());
        assertNotNull(publishedEvent.getArgument(0, UUID.class));
    }

    @Test
    void deregisterMerchant_ShouldRemoveMerchantAndPublishEvent() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant("1234567890", "Alice", "Smith", "merchant-account", "merchant-address");
        accountService.merchants.put(merchantId, merchant);

        // Act
        accountService.deregisterMerchant(merchantId);

        // Assert
        assertFalse(accountService.merchants.containsKey(merchantId));
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("MerchantDeregistered", publishedEvent.getTopic());
        assertTrue(publishedEvent.getArgument(0, boolean.class));
    }

    @Test
    void deregisterMerchant_ShouldPublishEventWithFalse() {
        // Arrange
        UUID merchantId = UUID.randomUUID();

        // Act
        accountService.deregisterMerchant(merchantId);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("MerchantDeregistered", publishedEvent.getTopic());
        assertFalse(publishedEvent.getArgument(0, boolean.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Disabled
    @Test
    void policyCustomerRegistrationRequested_ShouldCallCreateAccount() {
        // Arrange
        Customer customer = new Customer("1234567890", "John", "Doe", "account1", "address1");
        Event event = new Event("CustomerRegistrationRequested", customer);

        // Act
        accountService.policyCustomerRegistrationRequested(event);

        // Assert
        assertEquals(1, accountService.customers.size());
        verify(mockQueue).publish(any(Event.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Test
    void policyCustomerDeregistrationRequested_ShouldCallDeregisterCustomer() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("1234567890", "John", "Doe", "account1", "address1");
        accountService.customers.put(customerId, customer);
        Event event = new Event("CustomerDeregistrationRequested", customerId);

        // Act
        accountService.policyCustomerDeregistrationRequested(event);

        // Assert
        assertFalse(accountService.customers.containsKey(customerId));
        verify(mockQueue).publish(any(Event.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Test
    void policyCustomerTokensRequested_ShouldCallGetTokens() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("1234567890", "John", "Doe", "account1", "address1");
        accountService.customers.put(customerId, customer);
        Event event = new Event("CustomerTokensRequested", customerId);

        // Act
        accountService.policyCustomerTokensRequested(event);

        // Assert
        verify(mockQueue).publish(any(Event.class));
    }

    @Test
    void policyMerchantRegistrationRequested_ShouldCallCreateMerchantAccount() {
        // Arrange
        Merchant merchant = new Merchant("1234567890", "Alice", "Smith", "merchant-account", "merchant-address");
        Event event = new Event("MerchantRegistrationRequested", merchant);

        // Act
        accountService.policyMerchantRegistrationRequested(event);

        // Assert
        assertEquals(1, accountService.merchants.size());
        verify(mockQueue).publish(any(Event.class));
    }

    @Test
    void policyMerchantDeregistrationRequested_ShouldCallDeregisterMerchant() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant("1234567890", "Alice", "Smith", "merchant-account", "merchant-address");
        accountService.merchants.put(merchantId, merchant);
        Event event = new Event("MerchantDeregistrationRequested", merchantId);

        // Act
        accountService.policyMerchantDeregistrationRequested(event);

        // Assert
        assertFalse(accountService.merchants.containsKey(merchantId));
        verify(mockQueue).publish(any(Event.class));
    }

    @Test
    void policyValidateMerchant_ShouldPublishValidationResponse() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant("1234567890", "Alice", "Smith", "merchant-account", "merchant-address");
        accountService.merchants.put(merchantId, merchant);
        Event event = new Event("ValidateMerchant", merchantId);

        // Act
        accountService.policyValidateMerchant(event);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("MerchantValidationResponse", publishedEvent.getTopic());
        assertTrue(publishedEvent.getArgument(0, Boolean.class));
    }

    @Test
    void policyGetMerchantAccount_ShouldPublishAccountResponse() {
        // Arrange
        UUID merchantId = UUID.randomUUID();
        Merchant merchant = new Merchant("1234567890", "Alice", "Smith", "merchant-account", "merchant-address");
        accountService.merchants.put(merchantId, merchant);
        Event event = new Event("GetMerchantAccount", merchantId);

        // Act
        accountService.policyGetMerchantAccount(event);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("MerchantAccountResponse", publishedEvent.getTopic());
        assertEquals("merchant-account", publishedEvent.getArgument(0, String.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Test
    void policyGetCustomerAccount_ShouldPublishAccountResponse() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        Customer customer = new Customer("1234567890", "John", "Doe", "account1", "address1");
        accountService.customers.put(customerId, customer);
        Event event = new Event("GetCustomerAccount", customerId);

        // Act
        accountService.policyGetCustomerAccount(event);

        // Assert
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(mockQueue).publish(eventCaptor.capture());

        Event publishedEvent = eventCaptor.getValue();
        assertEquals("CustomerAccountResponse", publishedEvent.getTopic());
        assertEquals("account1", publishedEvent.getArgument(0, String.class));
    }
}
