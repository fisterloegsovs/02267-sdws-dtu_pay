package cucumber.merchant.models;

public record Merchant(
        String cpr,
        String firstName,
        String lastName,
        String account,
        String address) {
    public Merchant {

    }
}