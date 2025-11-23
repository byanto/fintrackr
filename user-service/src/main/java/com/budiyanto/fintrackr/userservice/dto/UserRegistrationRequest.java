package com.budiyanto.fintrackr.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationRequest(
        @NotBlank (message = "Username cannot be blank")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,

        @NotBlank (message = "Password cannot be blank")
        @Size(min = 6, max = 40, message = "Password must be between 6 and 40 characters") 
        String password,
        
        @NotBlank (message = "Email cannot be blank")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        @Email (message = "Email must be in valid format")
        String email

) {}
