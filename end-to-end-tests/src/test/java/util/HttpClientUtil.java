package utils;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

public class HttpClientUtil {

    private static final String BASE_URL = "http://localhost:8080/";

    private HttpClientUtil() {}

    /**
     * Creates and returns a WebTarget for the given path.
     *
     * @param path The specific path to target (e.g., "merchants/register").
     * @return WebTarget pointing to the specified path.
     */
    public static WebTarget getTarget(String path) {
        Client client = ClientBuilder.newClient();
        return client.target(BASE_URL).path(path);
    }
}
