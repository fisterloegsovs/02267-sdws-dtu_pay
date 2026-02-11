package cucumber.stepdefinitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import model.AccountService;
import model.Customer;
import model.Merchant;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

public class AccountServiceTestSteps {

    private AccountService accountService;
    private MessageQueue queue;
    private Merchant testMerchant;
    private Customer testCustomer;
    private UUID generatedMerchantId;
    private UUID generatedCustomerId;

    @Before
    public void setup() {
        queue = mock(MessageQueue.class);
        doAnswer(invocation -> {
            String topic = invocation.getArgument(0, String.class);
            @SuppressWarnings("unchecked")
            var handler = (java.util.function.Consumer<Event>) invocation.getArgument(1);

            if ("MerchantRegistered".equals(topic)) {
                handler.accept(new Event("MerchantRegistered", UUID.randomUUID()));
            }

            return null;
        }).when(queue).addHandler(anyString(), any());

        accountService = new AccountService(queue);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Given("a valid customer with details cpr {string}, first name {string}, last name {string}, account {string}, and address {string}")
    public void aValidCustomer(String cpr, String firstName, String lastName, String account, String address) {
        testCustomer = new Customer(cpr, firstName, lastName, account, address);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @When("the customer registration is initiated")
    public void customerRegistrationIsInitiated() {
        accountService.createAccount(testCustomer);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Then("the customer is successfully registered")
    public void customerIsSuccessfullyRegistered() {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals("CustomerRegistered", capturedEvent.getTopic());
        assertNotNull(capturedEvent.getArgument(0, UUID.class));
    }

    @Given("a valid merchant with details cpr {string}, first name {string}, last name {string}, account {string}, and address {string}")
    public void aValidMerchant(String cpr, String firstName, String lastName, String account, String address) {
        testMerchant = new Merchant(cpr, firstName, lastName, account, address);
    }

    @When("the merchant registration is initiated")
    public void merchantRegistrationIsInitiated() {
        accountService.createMerchantAccount(testMerchant);
    }

    @Then("the merchant is successfully registered")
    public void merchantIsSuccessfullyRegistered() {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals("MerchantRegistered", capturedEvent.getTopic());
        assertNotNull(capturedEvent.getArgument(0, UUID.class));
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Given("a customer with ID {string}")
    public void aCustomerWithID(String customerId) {
        generatedCustomerId = UUID.fromString(customerId);
        testCustomer = new Customer("1234567890", "Alice", "Smith", "merchant-account", "merchant-address");
        accountService.customers.put(generatedCustomerId, testCustomer);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @When("the customer deregistration is initiated")
    public void customerDeregistrationIsInitiated() {
        accountService.deregisterCustomer(generatedCustomerId);
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Then("the customer is successfully deregistered")
    public void customerIsSuccessfullyDeregistered() {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals("CustomerDeregistered", capturedEvent.getTopic());
    }

    @Given("a merchant with ID {string}")
    public void aMerchantWithID(String merchantId) {
        generatedMerchantId = UUID.fromString(merchantId);
        testMerchant = new Merchant("1234567890", "Alice", "Smith", "merchant-account", "merchant-address");
        accountService.merchants.put(generatedMerchantId, testMerchant);
    }

    @When("the merchant deregistration is initiated")
    public void merchantDeregistrationIsInitiated() {
        accountService.deregisterMerchant(generatedMerchantId);
    }

    @Then("the merchant is successfully deregistered")
    public void merchantIsSuccessfullyDeregistered() {
        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(queue).publish(eventCaptor.capture());
        Event capturedEvent = eventCaptor.getValue();
        assertEquals("MerchantDeregistered", capturedEvent.getTopic());
    }
}
