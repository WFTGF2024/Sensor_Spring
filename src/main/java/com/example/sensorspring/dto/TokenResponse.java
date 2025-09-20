package com.example.sensorspring.dto;

public class TokenResponse {
    private String accessToken; private String refreshToken; private String tokenType = "Bearer";
    public TokenResponse() {}
    public TokenResponse(String a, String r) { this.accessToken = a; this.refreshToken = r; }
    public String getAccessToken() { return accessToken; } public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; } public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getTokenType() { return tokenType; } public void setTokenType(String tokenType) { this.tokenType = tokenType; }
}
