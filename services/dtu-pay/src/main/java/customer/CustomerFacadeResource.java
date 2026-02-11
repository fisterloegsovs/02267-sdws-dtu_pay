package customer;

import customer.model.Customer;
import customer.model.TokenCreateRequest;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Path("/customer")
public class CustomerFacadeResource {
	
	@Inject
	CustomerFacade service;

    /**
     * @author Jonas Puidokas (137282)
     */
    @Path("register")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response registerCustomer(Customer customer) {
        try {
            String result = service.register(customer);
            if (result != null && !result.isEmpty()) {
                return Response.status(Response.Status.CREATED)
                        .entity(result)
                        .build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Customer registration failed")
                        .build();
            }
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Invalid customer data: " + e.getMessage())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An unexpected error occurred: " + e.getMessage())
                    .build();
        }
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Path("deregister/{customerId}")
    @DELETE
    public Response deregisterCustomer(@PathParam("customerId") UUID customerId) {
        try {
            boolean deregistered = service.deregister(customerId);
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

    /**
     * @author Jonas Puidokas (137282)
     */
    @POST
    @Path("/createTokens")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTokens(TokenCreateRequest tokenCreateRequest) {
        String result;
        boolean tokensCreated;
        try {
            tokensCreated = service.createTokens(tokenCreateRequest);

            result = tokensCreated ? "Tokens created" : "Tokens not created";
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        return Response.status(tokensCreated ? Response.Status.CREATED : Response.Status.BAD_REQUEST)
                .entity(result)
                .build();
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Path("getTokens/{customerId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTokens(@PathParam("customerId") UUID customerId) {
        try {
            List<UUID> tokenData = service.getTokens(customerId);

            if (tokenData != null) {
                return Response.ok(tokenData).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Customer not found")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving tokens: " + e.getMessage())
                    .build();
        }
    }

    /**
     * @author Jonas Puidokas (137282)
     */
    @Path("get-transactions/{customerId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomerTransactions(@PathParam("customerId") UUID customerId) {
        try {
            List<Object> customerTransactions = service.getCustomerTransactions(customerId);

            if (customerTransactions != null) {
                return Response.ok(customerTransactions).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Customer not found")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving customer transactions: " + e.getMessage())
                    .build();
        }
    }

}