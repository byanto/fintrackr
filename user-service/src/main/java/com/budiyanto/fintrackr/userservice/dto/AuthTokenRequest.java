package com.budiyanto.fintrackr.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthTokenRequest(
    @NotBlank(message = "Refresh token cannot be blank")
    String refreshToken
) {}
