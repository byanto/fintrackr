package com.budiyanto.fintrackr.userservice.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response object after a successful user login, containing user details and authentication tokens.")
public record UserLoginResponse(
        @Schema(description = "The unique identifier of the user.", example = "1")
        Long id,

        @Schema(description = "The username of the user.", example = "johndoe")
        String username,

        @Schema(description = "The email address of the user.", example = "john.doe@mail.com")
        String email,

        @Schema(description = "A list of roles assigned to the user.", example = "[\"ROLE_USER\"]")
        List<String> roles,

        @Schema(description = "The JWT access token used for authorizing requests.", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "The JWT refresh token used to obtain a new access token.", example = "eyJhbGcaskkaYxNiJ9...")
        String refreshToken,

        @Schema(description = "The type of token.", example = "Bearer")
        String tokenType
) {
        public UserLoginResponse(Long id, String username, String email, List<String> roles, String accessToken, String refreshToken) {
                this(id, username, email, roles, accessToken, refreshToken, "Bearer");
        }
}