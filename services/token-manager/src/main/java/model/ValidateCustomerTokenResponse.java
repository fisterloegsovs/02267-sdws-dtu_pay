package model;

import java.util.UUID;

public class ValidateCustomerTokenResponse {
    private UUID token;
    private String customerId;
    private String message;

    public ValidateCustomerTokenResponse(UUID token, String customerId, String message) {
        this.token = token;
        this.customerId = customerId;
        this.message = message;
    }

    // Getters and Setters
    public UUID getToken() {
        return token;
    }

    public void setToken(UUID token) {
        this.token = token;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ValidateCustomerTokenResponse{" +
                "token=" + token +
                ", customerId=" + customerId +
                ", message='" + message + '\'' +
                '}';
    }
}
