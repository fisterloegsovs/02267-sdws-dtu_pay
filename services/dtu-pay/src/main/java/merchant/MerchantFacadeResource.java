package merchant;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/merchant")
public class MerchantFacadeResource {

    @Inject
    MerchantFacade service;

    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String registerMerchant(Merchant merchant) throws InterruptedException, ExecutionException {
        return service.register(merchant);
    }

    @Path("deregister/{merchantId}")
    @DELETE
    public Response deregisterMerchant(@PathParam("merchantId") UUID merchantId) {
        try {
            boolean deregistered = service.deregister(merchantId);
            if (deregistered) {
                return Response.status(Response.Status.NO_CONTENT).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Customer not found")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while deregistering the customer")
                    .build();
        }
    }

    @Path("pay")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response initiatePayment(PaymentRequest request) throws InterruptedException, ExecutionException {
        PaymentResponse paymentResponse = service.initiatePayment(
                request.token(),
                request.merchantId(),
                request.amount(),
                request.description()
        );

        
        if (paymentResponse.isSuccess()) {
            return Response.ok(paymentResponse).build();
        } else {
            return Response.status(Response.Status.BAD_REQUEST).entity(paymentResponse).build();
        }
    }

    @Path("get-transactions/{merchantId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMerchantTransactions(@PathParam("merchantId") UUID merchantId) {
        try {
            List<Object> merchantTransactions = service.getMerchantTransactions(merchantId);

            if (merchantTransactions != null) {
                return Response.ok(merchantTransactions).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Merchant not found")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving merchant transactions: " + e.getMessage())
                    .build();
        }
    }


}

