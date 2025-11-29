package com.budiyanto.fintrackr.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Request object for updating user information")
public record UserUpdateRequest(

    @Schema(description = "The first name of the user.", example = "John")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    String firstName,

    @Schema(description = "The last name of the user.", example = "Doe")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    String lastName,

    @Schema(description = "The email of the user.", example = "john.doe@mail.com")
    @Email (message = "Email must be in valid format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String email

) {}
