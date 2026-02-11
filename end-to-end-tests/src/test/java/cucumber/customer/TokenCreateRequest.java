package cucumber.customer;


public class TokenCreateRequest {

    private String customerId;
    private Integer amount;

    public TokenCreateRequest(String customerId, int amount) {
        this.customerId = customerId;
        this.amount = amount;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
