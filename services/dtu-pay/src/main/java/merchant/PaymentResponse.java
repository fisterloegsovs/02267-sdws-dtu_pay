package merchant;

import java.util.UUID;

public class PaymentResponse {
    private boolean success;
    private String message;
    private UUID transactionId;

    public PaymentResponse(boolean success, String message, UUID transactionId) {
        this.success = success;
        this.message = message;
        this.transactionId = transactionId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public UUID getTransactionId() {
        return transactionId;
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

