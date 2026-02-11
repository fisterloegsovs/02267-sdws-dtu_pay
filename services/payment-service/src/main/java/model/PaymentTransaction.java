package model;

public record PaymentTransaction(
        String customerAccount,
        String merchantAccount,
        double amount,
        String description) {}

