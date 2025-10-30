package com.budiyanto.fintrackr.userservice.dto;

public record AuthTokenResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn
) {
    public AuthTokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}