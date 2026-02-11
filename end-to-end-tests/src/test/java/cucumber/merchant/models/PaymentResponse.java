package cucumber.merchant.models;

import java.util.UUID;

public class PaymentResponse {
    private boolean success;
    private String message;
    private UUID transactionId;


    public PaymentResponse() {
    }

    public PaymentResponse(boolean success, String message, UUID transactionId) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(UUID transactionId) {
        this.transactionId = transactionId;
    }

    @Override
    public String toString() {
        return "PaymentResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", transactionId=" + transactionId +
                '}';
    }
}
