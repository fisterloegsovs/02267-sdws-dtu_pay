package cucumber.merchant.models;

import java.util.UUID;

public class PaymentRequest {
    private UUID token;
    private UUID merchantId;
    private double amount;
    private String description;

    public PaymentRequest(UUID token, UUID merchantId, double amount, String description) {
        this.token = token;
        this.merchantId = merchantId;
        this.amount = amount;
        this.description = description;
    }

    public UUID getToken() {
        return token;
    }

    public UUID getMerchantId() {
        return merchantId;
    }

    public double getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
