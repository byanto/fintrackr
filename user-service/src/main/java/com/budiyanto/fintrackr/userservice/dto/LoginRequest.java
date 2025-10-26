package com.budiyanto.fintrackr.userservice.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank 
        String username,
        
        @NotBlank 
        String password
) {}
