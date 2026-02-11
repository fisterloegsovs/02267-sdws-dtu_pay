package cucumber.stepdefinitions;

import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException;
import dtu.ws.fastmoney.BankServiceException_Exception;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import messaging.Event;
import messaging.MessageQueue;
import services.PaymentService;
import org.junit.jupiter.api.Assertions;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.Mockito.*;

/**
 * @author Asher Sharif (240193)
 */
public class PaymentServiceTestSteps {

    private PaymentService paymentService;
    private MessageQueue queue;
    private BankService bankService;
    private String customerAccount;
    private String merchantAccount;
    private UUID token;
    private double amount;
    private String paymentResult;

    @Before
    public void setup() {
        queue = mock(MessageQueue.class);
        bankService = mock(BankService.class);
        paymentService = spy(new PaymentService(queue) {
            @Override
            public BankService getBankService() {
                return bankService;
            }
        });
    }

    @Given("a valid token {string}")
    public void givenAValidToken(String tokenString) {
        token = UUID.fromString(tokenString);
        doReturn("customer-id").when(paymentService).validateTokenAndGetCustomerId(token);
    }

    @Given("an invalid token {string}")
    public void givenAnInvalidToken(String tokenString) {
        token = UUID.fromString(tokenString);
        doReturn(null).when(paymentService).validateTokenAndGetCustomerId(token);
    }

    @Given("a valid customer account {string} and a merchant account {string}")
    public void givenValidCustomerAndMerchantAccounts(String customer, String merchant) {
        customerAccount = customer;
        merchantAccount = merchant;
        doReturn(customerAccount).when(paymentService).retrieveCustomerAccount("customer-id");
    }

    @Given("a customer account retrieval fails")
    public void givenCustomerAccountRetrievalFails() {
        doReturn(null).when(paymentService).retrieveCustomerAccount("customer-id");
    }

    @Given("a valid token but bank transfer fails")
    public void givenBankTransferFails() throws BankServiceException_Exception {
        doThrow(new BankServiceException_Exception("Bank transfer failed", new BankServiceException())).when(bankService)
                .transferMoneyFromTo(eq(customerAccount), eq(merchantAccount), eq(BigDecimal.valueOf(amount)), anyString());
    }

    @When("the payment is initiated with amount {double}")
    public void whenPaymentIsInitiated(double amount) {
        this.amount = amount;
        doAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            String eventName = event.getTopic();
            if ("PaymentSucceeded".equals(eventName)) {
                paymentResult = "PaymentSucceeded";
            } else if ("PaymentFailed".equals(eventName)) {
                paymentResult = (String) event.getArgument(1, String.class);
            }
            return null;
        }).when(queue).publish(any(Event.class));

        paymentService.policyPaymentRequested(new Event("PaymentRequested", new Object[]{
                token != null ? token : UUID.randomUUID(),
                UUID.randomUUID(),
                merchantAccount != null ? merchantAccount : "default-merchant-account",
                amount,
                "Test Description"
        }));
    }

    @Then("a payment succeeded event is published")
    public void thenPaymentSucceededEventIsPublished() {
        Assertions.assertEquals("PaymentSucceeded", paymentResult);
        verify(queue).publish(argThat(event -> event.getTopic().equals("PaymentSucceeded")));
    }

    @Then("a payment failed event is published with reason {string}")
    public void thenPaymentFailedEventIsPublished(String reason) {
        Assertions.assertEquals(reason, paymentResult);
        verify(queue).publish(argThat(event -> event.getTopic().equals("PaymentFailed") &&
                reason.equals(event.getArgument(1, String.class))));
    }

    @Given("a bank transfer failure is simulated")
    public void givenBankTransferFailsStep() throws BankServiceException_Exception {
        token = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        doReturn("customer-id").when(paymentService).validateTokenAndGetCustomerId(token);
        customerAccount = "customer-account";
        merchantAccount = "merchant-account";
        doReturn(customerAccount).when(paymentService).retrieveCustomerAccount("customer-id");
        doThrow(new BankServiceException_Exception("Simulated bank transfer failure", new BankServiceException()))
                .when(bankService)
                .transferMoneyFromTo(eq(customerAccount), eq(merchantAccount), eq(BigDecimal.valueOf(100.0)), anyString());
    }
}
