package model;

import java.util.UUID;

public record PaymentRequest(
        UUID token,
        UUID merchantId,
        String customerId,
        String customerAccount,
        String merchantAccount,
        double amount,
        String description) {}