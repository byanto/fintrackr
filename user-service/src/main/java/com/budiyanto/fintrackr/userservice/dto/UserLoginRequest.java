package com.budiyanto.fintrackr.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for user login")
public record UserLoginRequest(
        @Schema(description = "The username of the user.", 
                example = "johndoe",
                requiredMode =  Schema.RequiredMode.REQUIRED)
        @NotBlank (message = "Username cannot be blank")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,
        
        @Schema(description = "The password of the user.", 
                example = "password123",
                requiredMode =  Schema.RequiredMode.REQUIRED)
        @NotBlank (message = "Password cannot be blank")
        @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters") 
        String password
) {}
