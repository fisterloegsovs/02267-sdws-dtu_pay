package services;

import messaging.Event;
import messaging.MessageQueue;
import model.TokenCreateRequest;
import model.TokenDetails;
import model.ValidateCustomerTokenResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class TokenService {

    public ArrayList<TokenDetails> tokens = new ArrayList<TokenDetails>(); //Token, Valid

    private MessageQueue queue;
    private static final Logger logger = Logger.getLogger(TokenService.class.getName());

    //public TokenFacade() {
    //    queue = new RabbitMqQueue("rabbitmq");
    //}

    public TokenService(MessageQueue q) {
        this.queue = q;
        queue.addHandler("GenerateTokensForCustomer", this::policyGenerateTokensForCustomer);
        queue.addHandler("GetCustomerTokens", this::policyGetCustomerTokens);

        queue.addHandler("ValidateCustomerTokenRequest", this::policyValidateCustomerTokenRequest);
    }

    /// @Author
    public UUID generateToken(String userId) {
        var token = UUID.randomUUID();

        tokens.add(new TokenDetails(token, userId));
        return token;
    }

    /// @Author
    /// The integer should be between 1 and 5
    public void generateXTokens(String customerId, Integer tokenAmount) {
        boolean tokensGenerated = false;
        logger.info("Generating tokens for customerId = " + customerId + " with token amount = " + tokenAmount);
        long userTokensCount = tokens.stream()
                .filter(t -> t.getUserId().equals(customerId) && t.getIsValid())
                .count();

        System.out.println(userTokensCount);

        if (tokenAmount <= 0 || tokenAmount > 5 || userTokensCount+tokenAmount > 6) {
            if(tokenAmount<=0) logger.info("The amount of tokens cannot be less than 1");
            if(tokenAmount>5) logger.info("The amount of tokens cannot be greater than 5");

            if(userTokensCount+tokenAmount > 6) {
                logger.info("You currently have " + userTokensCount + " tokens, and therefor cannot request " + tokenAmount + " tokens");
            }
        } else {
            tokensGenerated = true;
            for(int i=0; i<tokenAmount; i++) {
                generateToken(customerId);
            }
        }

        queue.publish(new Event("XTokensRequest", new Object[]{tokensGenerated}));
    }

    /// @Author
    public boolean checkToken(UUID token) throws IllegalArgumentException {
        if(token==null) throw new IllegalArgumentException("The token cannot be null");

        TokenDetails tokenDetails = null;
        for(TokenDetails t : tokens) {
            if(t.getToken().equals(token) && t.getIsValid()) {
                tokenDetails = t;
            }
        }
        if (tokenDetails == null) throw new IllegalArgumentException("The token does not exist or is invalid");


        tokens.remove(tokenDetails);
        tokenDetails.setIsValid(false);
        tokens.add(tokenDetails);

        queue.publish(new Event("ValidateToken", new Object[]{token}));
        return true;
    }

    /// @Author
    public boolean removeToken(UUID token) {
        if(token==null) return true;

        TokenDetails tokenDetails = null;
        for(TokenDetails t : tokens) {
            if(t.getToken().equals(token) && t.getIsValid()) {
                tokenDetails = t;
            }
        }
        if (tokenDetails == null) return false;

        tokens.remove(tokenDetails);
        tokenDetails.setIsValid(false);
        tokens.add(tokenDetails);

        queue.publish(new Event("RemoveToken", new Object[]{token}));
        return true;
    }


    /** Policies */

    private void policyGenerateTokensForCustomer(Event event) {
        TokenCreateRequest request = event.getArgument(0, TokenCreateRequest.class);
        try {
            generateXTokens(request.getCustomerId(), request.getAmount());
        } catch (IllegalArgumentException e) {
            e.getMessage();
        }
    }

    private void policyGetCustomerTokens(Event event) {
        logger.info("Get cust token function");
        UUID customerId = event.getArgument(0, UUID.class);
        logger.info("Getting tokens for cust:"+customerId);
        String userId = customerId.toString();
        List<UUID> validTokens = tokens.stream()
                .filter(t -> t.getUserId().equals(userId) && t.getIsValid())
                .map(TokenDetails::getToken)
                .toList();
        logger.info("valid tokens are:"+validTokens.toString());
        queue.publish(new Event("CustomerTokensResponse", validTokens));
    }

    private void policyValidateTokenRequest(Event event) {
        UUID token = event.getArgument(0, UUID.class);
        String eventKey = "ValidateTokenResponse_"+token;
        logger.info("the token to valid is:"+token);
        String customerId = null;
        boolean isValid = false;
        String errorMessage = null;
        try {
            TokenDetails tokenDetails = tokens.stream()
                    .filter(t -> t.getToken().equals(token) && t.getIsValid())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Token not valid or does not exist"));
            customerId = tokenDetails.getUserId();
            isValid = true;
            tokenDetails.setIsValid(false);
        } catch (IllegalArgumentException e) {
            logger.severe("In the catch block exception is:"+e.getMessage());
            errorMessage = e.getMessage();
        }

        queue.publish(new Event(eventKey, new Object[]{token, isValid, customerId, errorMessage}));
        logger.info("publising validation response:"+token+" and "+ isValid+" and "+ customerId+" and "+ errorMessage);
    }

    private void policyValidateCustomerTokenRequest(Event event) {
        UUID token = event.getArgument(0, UUID.class);
        String customerId = null;
        String message = null;
        try {
            TokenDetails tokenDetails = tokens.stream()
                    .filter(t -> t.getToken().equals(token) && t.getIsValid())
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Token not valid or does not exist"));
            customerId = tokenDetails.getUserId();
            tokenDetails.setIsValid(false);
            message = "Token validated successfully.";
            logger.info("Token validation succeeded for token: " + token);
        } catch (IllegalArgumentException e) {
            logger.severe("Exception during token validation: " + e.getMessage());
            message = e.getMessage();
        }
        ValidateCustomerTokenResponse response = new ValidateCustomerTokenResponse(token, customerId, message);
        queue.publish(new Event("ValidateCustomerTokenResponse", new Object[]{response}));
        logger.info("Published validation response: " + response);
    }

}
