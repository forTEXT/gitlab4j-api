package org.gitlab4j.api.models;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class OauthTokenResponse {

    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
    private String refreshToken;
    private String scope;
    private Long createdAt;

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Integer expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }


    @JsonIgnore
    public Date getExpiresAt() {
        if (expiresIn == null) {
            return null;
        }

        long expiryTimeInSecondsSinceEpoch = createdAt + expiresIn;
        return new Date(expiryTimeInSecondsSinceEpoch * 1000);
    }

    @JsonIgnore
    public boolean isExpired() {
        Date expiresAt = getExpiresAt();
        if (expiresAt == null) {
            return false;
        }

        return new Date().after(expiresAt);
    }
}
