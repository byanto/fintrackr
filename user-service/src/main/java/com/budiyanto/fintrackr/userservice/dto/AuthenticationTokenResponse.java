package com.budiyanto.fintrackr.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing access and refresh tokens after a successful authentication")
public record AuthenticationTokenResponse(
    @Schema(description = "The JWT access token used for authorizing requests", 
            example = "eyJhbGciOiJIUzI1...")
    String accessToken,
    
    @Schema(description = "The JWT refresh token used for renewing the access token", 
            example = "eyJhbGciOiJIUzI...")
    String refreshToken,
    
    @Schema(description = "The type of token", 
            example = "Bearer")
    String tokenType,

    @Schema(description = "The lifetime of the access token in seconds.",
            example = "3600")
    long expiresIn
) {
    public AuthenticationTokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}