package merchant;

import java.util.UUID;

public record PaymentRequest(
        UUID token,
        UUID merchantId,
        double amount,
        String description) {}