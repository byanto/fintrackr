package com.budiyanto.fintrackr.userservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budiyanto.fintrackr.userservice.dto.ErrorResponse;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.dto.UserUpdateRequest;
import com.budiyanto.fintrackr.userservice.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "Endpoints for retrieving user information")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class UserController {
    
    private final UserService userService;

    @Operation(summary = "Get current user's profile", description = "Retrieves the profile information of the currently authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - The user is not authenticated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Full authentication is required to access this resource\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}")))
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(Authentication authentication) {
        UserResponse user = userService.getUserByUsername(authentication.getName());
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update current user's profile", description = "Updates the profile information of the currently authenticated user.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated user profile",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input, e.g., email has wrong format",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Validation Failed\",\"status\":400,\"detail\":\"One or more fields have an error.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\",\"errors\":{\"email\":\"Email should be valid\"}}"))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - The user is not authenticated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"/errors/invalid-credentials\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Full authentication is required to access this resource\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}"))),
        @ApiResponse(responseCode = "404", description = "Not Found - The user is not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"/errors/user-not-found\",\"title\":\"User Not Found\",\"status\":404,\"detail\":\"The user is not found.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}"))),
        @ApiResponse(responseCode = "409", description = "Conflict - The provided email is already in use by another account",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"/errors/user-already-exists\",\"title\":\"User Already Exists\",\"status\":409,\"detail\":\"Email otheruser@mail.com is already in use.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}")))
    })
    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateMe(Authentication authentication, @Valid @RequestBody UserUpdateRequest request) {
        UserResponse updatedUser = userService.updateUser(authentication.getName(), request);
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Get user profile by username", description = "Retrieves the profile information for a specific user. Requires ADMIN role or for the user to be accessing their own profile.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved user profile",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized - The user is not authenticated",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Unauthorized\",\"status\":401,\"detail\":\"Full authentication is required to access this resource\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}"))),
        @ApiResponse(responseCode = "403", description = "Forbidden - The user is not authorized to access this resource",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"about:blank\",\"title\":\"Forbidden\",\"status\":403,\"detail\":\"Access Denied\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}"))),
        @ApiResponse(responseCode = "404", description = "User not found",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class),
            examples = @ExampleObject(value = "{\"type\":\"/errors/user-not-found\",\"title\":\"User Not Found\",\"status\":404,\"detail\":\"User with username 'nonexistent' not found.\",\"timestamp\":\"2025-11-27T15:59:08.123Z\"}")))
    })
    @GetMapping("/{username}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.principal.username")
    public ResponseEntity<UserResponse> getUserByUsername(
            @PathVariable @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters") String username) {
        
        UserResponse user = userService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    
    }

}
