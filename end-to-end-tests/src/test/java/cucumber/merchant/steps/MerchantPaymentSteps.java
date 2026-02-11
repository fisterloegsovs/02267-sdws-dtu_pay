package cucumber.merchant.steps;

import cucumber.customer.Customer;
import cucumber.customer.CustomerFacade;
import cucumber.customer.TokenCreateRequest;
import cucumber.merchant.models.*;
import cucumber.merchant.services.MerchantService;
import dtu.ws.fastmoney.Account;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

public class MerchantPaymentSteps {

    private BankService bankService;
    private MerchantService merchantService;
    private CustomerFacade customerService;

    private String customerId;
    private String merchantId;
    private String customerBankAccount;
    private String merchantBankAccount;

    List<String> accounts = new ArrayList<>();
    private List<String> tokens;
    private PaymentResponse paymentResponse;
    String customerCPR = "345091-1151";
    String merchantCPR = "211091-1150";
    private static final Logger logger = Logger.getLogger(MerchantPaymentSteps.class.getName());

    public MerchantPaymentSteps() {
        bankService = new BankServiceService().getBankServicePort();
        merchantService = new MerchantService();
        customerService = new CustomerFacade();
    }

    @Before
    public void prepareBank() throws BankServiceException_Exception {
        try {
            Account custBankAccount = bankService.getAccountByCprNumber(customerCPR);
            bankService.retireAccount(custBankAccount.getId());
            Account merchBankAccount = bankService.getAccountByCprNumber(merchantCPR);
            bankService.retireAccount(merchBankAccount.getId());
        } catch (BankServiceException_Exception e) {
            // Everything ok
        }
    }

    @Given("a customer with a valid bank account with sufficient amount and valid token")
    public void givenCustomerWithValidBankAccountAndToken() throws BankServiceException_Exception {
        try{
        var user = new dtu.ws.fastmoney.User();
        user.setCprNumber(customerCPR);
        user.setFirstName("Alice");
        user.setLastName("Johnson");
        customerBankAccount = bankService.createAccountWithBalance(user, new BigDecimal(1000));
        assertNotNull(customerBankAccount);
        accounts.add(customerBankAccount);
        logger.info("Customer bank account created"+customerBankAccount);

        Customer customer = new Customer("2110911234", "Alice", "Johnson", customerBankAccount, "123 Elm Street");
        customerId = customerService.register(customer);
        assertNotNull(customerId);
        logger.info("Customer dtu pay account created"+customerId);

        customerService.createTokens(new TokenCreateRequest(customerId, 5));
        logger.info("tokens created");
        Response tokensResponse = customerService.getTokens(customerId);
        assertEquals(Response.Status.OK.getStatusCode(), tokensResponse.getStatus());
        tokens = tokensResponse.readEntity(new GenericType<List<String>>() {});
        assertNotNull(tokens);
        assertFalse(tokens.isEmpty());
        logger.info("tokens created are:"+tokens);
        } catch (Exception e) {
            logger.info("Exceptions are:"+e.getMessage());
        }
    }

    @Given("a merchant with a valid bank account")
    public void givenMerchantWithValidBankAccount() throws BankServiceException_Exception {
        var user = new dtu.ws.fastmoney.User();
        user.setCprNumber(merchantCPR);
        user.setFirstName("Bob");
        user.setLastName("Smith");
        merchantBankAccount = bankService.createAccountWithBalance(user, new BigDecimal(5000));
        assertNotNull(merchantBankAccount);
        accounts.add(merchantBankAccount);

        Merchant merchant = new Merchant("3107904321", "Bob", "Smith", merchantBankAccount, "456 Maple Avenue");
        merchantId = merchantService.registerMerchant(merchant);
        assertNotNull(merchantId);
    }

    @When("the merchant initiates a payment request with the customer's token")
    public void whenMerchantInitiatesPaymentRequest() {
        UUID token = UUID.fromString(tokens.get(0));

        PaymentRequest paymentRequest = new PaymentRequest(
                token,
                UUID.fromString(merchantId),
                100.00,
                "Payment for services"
        );

        Response response = merchantService.initiatePayment(paymentRequest);
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

        paymentResponse = response.readEntity(PaymentResponse.class);
        assertNotNull(paymentResponse);
    }

    @Then("the payment is successfully processed")
    public void thenPaymentIsSuccessfullyProcessed() {
        assertTrue(paymentResponse.isSuccess());
        assertNotNull(paymentResponse.getTransactionId());
        assertEquals("Payment succeeded", paymentResponse.getMessage());
    }

    @Then("the merchant receives the amount")
    public void thenMerchantReceivesAmount() throws BankServiceException_Exception {
        BigDecimal merchantBalance = bankService.getAccountByCprNumber(merchantCPR).getBalance();
        assertEquals(0, merchantBalance.compareTo(new BigDecimal("5100")));
    }

    @Then("the customer's balance is reduced by the payment amount")
    public void thenCustomerBalanceReduced() throws BankServiceException_Exception {
        BigDecimal customerBalance = bankService.getAccountByCprNumber(customerCPR).getBalance();
        assertEquals(0, customerBalance.compareTo(new BigDecimal("900")));
    }

    @Then("the merchant can generate a transaction report")
    public void thenMerchantCanGenerateTransactionReport() {
        Response response = merchantService.getMerchantTransactions(UUID.fromString(merchantId));
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        List<Object> transactions = response.readEntity(new GenericType<List<Object>>() {});
        assertNotNull(transactions);
        assertFalse(transactions.isEmpty());
    }


    @After
    public void cleanupAccounts() {
        for (var account : accounts) {
            try {
                bankService.retireAccount(account);
            } catch (BankServiceException_Exception e) {
                System.err.println("Failed to retire bank account: " + account + ". Error: " + e.getMessage());
            }
        }
        if (merchantId != null && !merchantId.isEmpty()) {
            try {
                merchantService.deregisterMerchant(UUID.fromString(merchantId));
            } catch (RuntimeException e) {
                System.err.println("Failed to deregister merchant with ID: " + merchantId + ". Error: " + e.getMessage());
            }
        }
        if (customerId != null && !customerId.isEmpty()) {
            try {
                customerService.deregister(customerId);
            } catch (RuntimeException e) {
                System.err.println("Failed to deregister merchant with ID: " + merchantId + ". Error: " + e.getMessage());
            }
        }
    }

}
