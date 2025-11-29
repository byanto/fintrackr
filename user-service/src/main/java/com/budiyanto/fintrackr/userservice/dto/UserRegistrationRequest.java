package com.budiyanto.fintrackr.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for new user registration")
public record UserRegistrationRequest(
        @Schema(description = "The username of the user, must be unique.", 
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
        String password,

        @Schema(description = "The first name of the user.", example = "John")
        @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
        String firstName,

        @Schema(description = "The last name of the user.", example = "Doe")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
        String lastName,
        
        @Schema(description = "The email of the user, must be unique and a valid format.", 
                example = "john.doe@mail.com",
                requiredMode =  Schema.RequiredMode.REQUIRED)
        @NotBlank (message = "Email cannot be blank")
        @Email (message = "Email must be in valid format")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email

) {}
