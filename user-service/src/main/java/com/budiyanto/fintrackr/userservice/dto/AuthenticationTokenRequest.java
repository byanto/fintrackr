package com.budiyanto.fintrackr.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for renewing an access token using a refresh token.")
public record AuthenticationTokenRequest(
    @Schema(description = "The username of the user requesting the token renewal.",
            example = "johndoe",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    String username,
    
    @Schema(description = "The valid refresh token.",
            example = "eyJhbGciOiJIUzI1NiJ9...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Refresh token cannot be blank")
    String refreshToken
) {}
