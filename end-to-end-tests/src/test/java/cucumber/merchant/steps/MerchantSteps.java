package cucumber.merchant.steps;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import cucumber.merchant.models.Merchant;
import cucumber.merchant.services.MerchantService;
import dtu.ws.fastmoney.Account;
import dtu.ws.fastmoney.BankService;
import dtu.ws.fastmoney.BankServiceException_Exception;
import dtu.ws.fastmoney.BankServiceService;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class MerchantSteps {

    private BankService bankService;
    private MerchantService merchantService;
    private String bankAccountId;
    private String merchantId;
    List<String> accounts = new ArrayList<>();
    String cpr = "211091-1150";

    public MerchantSteps() {
        bankService = new BankServiceService().getBankServicePort();
        merchantService = new MerchantService();
    }

    @Before
    public void prepareBank() throws BankServiceException_Exception {
        try {
            Account bankAccount = bankService.getAccountByCprNumber(cpr);
            bankService.retireAccount(bankAccount.getId());
        } catch (BankServiceException_Exception e) {
            // Everything ok
        }
    }

    @Given("a merchant with a bank account")
    public void aMerchantWithABankAccount() throws BankServiceException_Exception {
        var user = new dtu.ws.fastmoney.User();
        user.setCprNumber(cpr); 
        user.setFirstName("John");
        user.setLastName("Doe");
        bankAccountId = bankService.createAccountWithBalance(user, new BigDecimal(1000));
        accounts.add(bankAccountId);
        assertNotNull(bankAccountId);
    }

    @When("the merchant registers with DTU Pay")
    public void theMerchantRegistersWithDTUPay() {
        Merchant merchant = new Merchant(cpr, "John", "Doe", bankAccountId, "Some Address");
        merchantId = merchantService.registerMerchant(merchant);
        assertNotNull(merchantId);
    }

    @Then("the merchant is successfully registered")
    public void theMerchantIsSuccessfullyRegistered() {
        assertNotNull(merchantId);
        assertFalse(merchantId.isEmpty());
    }

    @When("the merchant deregisters from DTU Pay")
    public void theMerchantDeregistersFromDTUPay() {
        UUID merchantUUID = UUID.fromString(merchantId);
        boolean deregistered = merchantService.deregisterMerchant(merchantUUID);
        assertTrue(deregistered);
    }

    @Then("the merchant is successfully deregistered")
    public void theMerchantIsSuccessfullyDeregistered() {
        try {
            UUID merchantUUID = UUID.fromString(merchantId);
            merchantService.deregisterMerchant(merchantUUID);
            fail("Deregistration of a non-existent merchant should throw an exception.");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Merchant not found"), "Expected error for non-existent merchant.");
        }
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
    }

}
