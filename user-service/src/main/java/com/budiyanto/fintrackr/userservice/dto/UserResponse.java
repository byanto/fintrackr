package com.budiyanto.fintrackr.userservice.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object containing public information about a user.")
public record UserResponse(
    @Schema(description = "The unique identifier of the user.", example = "1")
    Long id,

    @Schema(description = "The username of the user.", example = "johndoe")
    String username,

    @Schema(description = "The first name of the user.", example = "John")
    String firstName,

    @Schema(description = "The last name of the user.", example = "Doe")
    String lastName,

    @Schema(description = "The email address of the user.", example = "john.doe@mail.com")
    String email,

    @Schema(description = "The timestamp when the user account was created.")
    Instant createdAt
) {}
