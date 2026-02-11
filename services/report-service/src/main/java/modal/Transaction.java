package modal;

import java.util.UUID;

public class Transaction {
    private UUID transactionId;
    private UUID token;
    private UUID merchantId;
    private String customerId;
    private String customerAccount;
    private String merchantAccount;
    private double amount;
    private String description;
    private String status;

    public Transaction(UUID transactionId, UUID token, UUID merchantId, String customerId, String customerAccount, String merchantAccount, double amount, String description, String status) {
        this.transactionId = transactionId;
        this.token = token;
        this.merchantId = merchantId;
        this.customerId = customerId;
        this.customerAccount = customerAccount;
        this.merchantAccount = merchantAccount;
        this.amount = amount;
        this.description = description;
        this.status = status;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(UUID merchantId) {
        this.merchantId = merchantId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerAccount() {
        return customerAccount;
    }

    public void setCustomerAccount(String customerAccount) {
        this.customerAccount = customerAccount;
    }

    public String getMerchantAccount() {
        return merchantAccount;
    }

    public void setMerchantAccount(String merchantAccount) {
        this.merchantAccount = merchantAccount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId=" + transactionId +
                ", token=" + token +
                ", merchantId=" + merchantId +
                ", customerAccount='" + customerAccount + '\'' +
                ", merchantAccount='" + merchantAccount + '\'' +
                ", amount=" + amount +
                ", description='" + description + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
