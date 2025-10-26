package com.budiyanto.fintrackr.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank 
        @Size(min = 3, max = 40)
        String username,
        
        @NotBlank 
        @Email 
        String email,
        
        @NotBlank 
        @Size(min = 6, max = 20) 
        String password
) {}
