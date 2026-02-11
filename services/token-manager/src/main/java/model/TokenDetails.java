package model;

import java.util.UUID;

public class TokenDetails {
    private UUID token;
    private String userId;
    private Boolean isValid;

    public TokenDetails(UUID token, String userId) {
        this.token = token;
        this.userId = userId;
        this.isValid = true;
    }

    /// @Author s194309
    public UUID getToken() {
        return token;
    }

    /// @Author s194309
    public void setToken(UUID token) {
        this.token = token;
    }

    /// @Author s194309
    public String getUserId() {
        return userId;
    }

    /// @Author s233470
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /// @Author s233470
    public Boolean getIsValid() {
        return isValid;
    }

    /// @Author s233470
    public void setIsValid(Boolean isValid) {
        this.isValid = isValid;
    }


}
