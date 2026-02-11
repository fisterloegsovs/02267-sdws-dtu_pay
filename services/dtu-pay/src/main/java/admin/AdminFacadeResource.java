package admin;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * @author Asher Sharif (240193)
 */
@Path("/admin")
public class AdminFacadeResource {

    @Inject
    AdminFacade service;

    @Path("get-transactions")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTransactions() {
        try {
            List<Object> allTransactions = service.getAllTransactions();

            if (allTransactions != null) {
                return Response.ok(allTransactions).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("No transactions found")
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while retrieving all transactions: " + e.getMessage())
                    .build();
        }
    }

}

