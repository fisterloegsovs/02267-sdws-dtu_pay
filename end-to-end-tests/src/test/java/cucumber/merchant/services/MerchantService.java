package cucumber.merchant.services;

import java.util.UUID;

import cucumber.merchant.models.Merchant;
import cucumber.merchant.models.PaymentRequest;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import utils.HttpClientUtil;

public class MerchantService {

    private static final String BASE_PATH = "merchant";

    /**
     * Registers a merchant with DTU Pay.
     *
     * @param merchant The Merchant object containing the registration details.
     * @return The merchant ID as a String if registration is successful.
     * @throws RuntimeException if the registration fails.
     */
    public String registerMerchant(Merchant merchant) {
        WebTarget target = HttpClientUtil.getTarget(BASE_PATH + "/register");
        Response response = target.request().post(Entity.json(merchant));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            return response.readEntity(String.class);
        } else {
            throw new RuntimeException("Failed to register merchant: " + response.getStatusInfo().getReasonPhrase());
        }
    }

    /**
     * Deregisters a merchant from DTU Pay.
     *
     * @param merchantId The UUID of the merchant to deregister.
     * @return True if deregistration is successful, false otherwise.
     */
    public boolean deregisterMerchant(UUID merchantId) {
        WebTarget target = HttpClientUtil.getTarget(BASE_PATH + "/deregister/" + merchantId);
        Response response = target.request().delete();

        if (response.getStatus() == Response.Status.NO_CONTENT.getStatusCode()) {
            // Merchant successfully deregistered
            return true;
        } else if (response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()) {
            throw new RuntimeException("Merchant not found: " + merchantId);
        } else {
            throw new RuntimeException("Failed to deregister merchant: " + response.getStatusInfo().getReasonPhrase());
        }
    }

    /**
     * Initiates a payment request through DTU Pay.
     *
     * @param paymentRequest The PaymentRequest object containing token, merchant ID, amount, and description.
     * @return The PaymentResponse indicating the success or failure of the payment.
     * @throws RuntimeException if the payment initiation fails.
     */
    public Response initiatePayment(PaymentRequest paymentRequest) {
        Response response = HttpClientUtil.getTarget(BASE_PATH + "/pay")
                .request()
                .post(Entity.json(paymentRequest));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            return response;
        } else {
            throw new RuntimeException("Failed to initiate payment: " + response.getStatusInfo().getReasonPhrase());
        }
    }

    /**
     * Retrieves the transaction details for a specific merchant.
     * @param merchantId
     * @return A Response object containing the transaction detail
     */
    public Response getMerchantTransactions(UUID merchantId) {
        return HttpClientUtil.getTarget("merchant/get-transactions/" + merchantId)
                .request()
                .get();
    }
}
