package com.budiyanto.fintrackr.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.ErrorResponse;
import com.budiyanto.fintrackr.userservice.dto.UserLoginRequest;
import com.budiyanto.fintrackr.userservice.dto.UserLoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.service.AuthenticationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and token renewal")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user", 
                description = "Registers a new user with the provided credentials")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "User registered successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input, e.g., username or email has wrong format",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Validation Failed\",\"status\":400,\"detail\":\"One or more fields have an error.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\",\"errors\":{\"username\":\"Username is required\"}}"))),
        @ApiResponse(responseCode = "404", description = "Default role not found on the server",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"type\":\"/errors/role-not-found\",\"title\":\"Role Not Found\",\"status\":404,\"detail\":\"Default role ROLE_USER not found.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}"))),
        @ApiResponse(responseCode = "409", description = "Username or email already exists",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"type\":\"/errors/user-already-exists\",\"title\":\"User Already Exists\",\"status\":409,\"detail\":\"Username 'johndoe' is already taken.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}")))
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        UserResponse registeredUser = authenticationService.registerUser(request);

        // Create the location URI for the new user resource
        java.net.URI location = ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/users/{username}")
                .buildAndExpand(registeredUser.username()).toUri();

        // Return 201 Created with the Location header
        return ResponseEntity.created(location).body(registeredUser);
    }

    @Operation(summary = "Authenticate a user", 
                description = "Logs in a user with username and password, returning authentication tokens.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User authenticated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserLoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input, e.g., missing username or password",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Validation Failed\",\"status\":400,\"detail\":\"One or more fields have an error.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\",\"errors\":{\"password\":\"Password is required\"}}"))),
        @ApiResponse(responseCode = "401", description = "Invalid username or password",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"type\":\"/errors/invalid-credentials\",\"title\":\"Invalid Credentials\",\"status\":401,\"detail\":\"Invalid username or password\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        UserLoginResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Renew an access token",
            description = "Uses a valid refresh token to issue a new access token and refresh token"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token renewed successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthenticationTokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input, e.g., missing username or refreshToken",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Validation Failed\",\"status\":400,\"detail\":\"One or more fields have an error.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\",\"errors\":{\"refreshToken\":\"Refresh token cannot be blank\"}}"))),
        @ApiResponse(responseCode = "403", description = "The provided refresh token is invalid, not found, or has expired",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"/errors/invalid-refresh-token\",\"title\":\"Invalid Refresh Token\",\"status\":403,\"detail\":\"The provided refresh token is invalid, not found, or expired.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}")))
    })
    @PostMapping("/renewtoken")
    public ResponseEntity<AuthenticationTokenResponse> renewAuthToken(@Valid @RequestBody AuthenticationTokenRequest request) {
        AuthenticationTokenResponse response = authenticationService.renewAuthToken(request);
        return ResponseEntity.ok(response);
    }

}
