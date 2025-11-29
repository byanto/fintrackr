package com.budiyanto.fintrackr.userservice.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.dto.UserUpdateRequest;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.exception.UserNotFoundException;
import com.budiyanto.fintrackr.userservice.security.JwtAuthenticationFilter;
import com.budiyanto.fintrackr.userservice.security.JwtService;
import com.budiyanto.fintrackr.userservice.security.SecurityBeansConfig;
import com.budiyanto.fintrackr.userservice.security.SecurityConfig;
import com.budiyanto.fintrackr.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@Import({SecurityConfig.class, SecurityBeansConfig.class, JwtAuthenticationFilter.class})
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("GET /api/users/me")
    class GetMeEndpoint {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("should return 200 OK with user details for authenticated user")
        void should_return200Ok_when_userIsAuthenticated() throws Exception {
            // Arrange
            String username = "testuser";
            String firstName = "John";
            String lastName = "Doe";
            String email = "test@email.com";
            UserResponse userResponse = new UserResponse(1L, username, firstName, lastName, email, Instant.now());
            when(userService.getUserByUsername(username)).thenReturn(userResponse);

            // Act & Assert
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$.lastName").value(lastName))
                    .andExpect(jsonPath("$.email").value(email));
        }

        @Test
        @DisplayName("should return 401 Unauthorized when user is not authenticated")
        void should_return401Unauthorized_when_userIsNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/me")
    class UpdateMeEndpoint {

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("should return 200 OK with updated user details for authenticated user")
        void should_return200Ok_when_userIsAuthenticated() throws Exception {
            // Arrange
            String username = "testuser";
            String firstName = "John";
            String lastName = "Doe";
            String email = "test@email.com";

            UserUpdateRequest request = new UserUpdateRequest(firstName, lastName, email);
            UserResponse userResponse = new UserResponse(1L, username, firstName, lastName, email, Instant.now());
            when(userService.updateUser(username, request)).thenReturn(userResponse);

            // Act & Assert
            mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$.lastName").value(lastName))
                    .andExpect(jsonPath("$.email").value(email));

        }

        @ParameterizedTest
        @MethodSource("provideInvalidUserUpdateRequests")
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("should return 400 BadRequest when any field is invalid")
        void should_return400BadRequest_when_requestIsInvalid(UserUpdateRequest invalidRequest) throws Exception {
            // Act & Assert
            mockMvc.perform(put("/api/users/me")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<UserUpdateRequest> provideInvalidUserUpdateRequests() {
            return Stream.of(
                new UserUpdateRequest("a", "Doe", "test@email.com"),
                new UserUpdateRequest("John", "D", "test@email.com"),
                new UserUpdateRequest("John", "Doe", "invalidemailformat")
            );
        }

        @Test
        @DisplayName("should return 401 Unauthorized when user is not authenticated")
        void should_return401Unauthorized_when_userIsNotAuthenticated() throws Exception {
            // Arrange
            UserUpdateRequest request = new UserUpdateRequest("John", "Doe", "test@email.com");

            // Act & Assert
            mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "nonExistentUser")
        @DisplayName("should return 404 NotFound when user is not found")
        void should_return404NotFound_when_userIsNotFound() throws Exception {
            // Arrange
            String nonExistentUser = "nonExistentUser";
            UserUpdateRequest request = new UserUpdateRequest("John", "Doe", "test@email.com");
            when(userService.updateUser(nonExistentUser, request))
                .thenThrow(new UserNotFoundException("User with username \"%s\" is not found.".formatted(nonExistentUser)));
            
            // Act & Assert
            mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("should return 409 Conflict when email is taken")
        void should_return409Conflict_when_emailIsTaken() throws Exception {
            // Arrange
            String username = "testuser";
            String newEmail = "taken@email.com";
            UserUpdateRequest request = new UserUpdateRequest("John", "Doe", newEmail);
            when(userService.updateUser(username, request))
                .thenThrow(new UserAlreadyExistsException("Email %s is already in use.".formatted(newEmail)));

             // Act & Assert
            mockMvc.perform(put("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        } 
    }

    @Nested
    @DisplayName("GET /api/users/{username}")
    class GetUserByUsernameEndpoint { 
        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("should return 200 OK when user accesses their own profile")
        void should_return200Ok_when_userAccessesOwnProfile() throws Exception {
            // Arrange
            String username = "testuser";
            String firstName = "John";
            String lastName = "Doe";
            String email = "test@email.com";

            UserResponse userResponse = new UserResponse(1L, username, firstName, lastName, email, Instant.now());
            when(userService.getUserByUsername(username)).thenReturn(userResponse);

            // Act & Assert
            mockMvc.perform(get("/api/users/{username}", username))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(username))
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$.lastName").value(lastName))
                    .andExpect(jsonPath("$.email").value(email));
        }

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        @DisplayName("should return 200 OK when admin accesses another user's profile")
        void should_return200Ok_when_adminAccessesOtherProfile() throws Exception {
            // Arrange
            String targetUsername = "otheruser";
            String firstName = "John";
            String lastName = "Doe";
            String email = "other@email.com";
            UserResponse userResponse = new UserResponse(2L, targetUsername, firstName, lastName, email, Instant.now());
            when(userService.getUserByUsername(targetUsername)).thenReturn(userResponse);

            // Act & Assert
            mockMvc.perform(get("/api/users/{username}", targetUsername))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value(targetUsername))
                    .andExpect(jsonPath("$.firstName").value(firstName))
                    .andExpect(jsonPath("$.lastName").value(lastName))
                    .andExpect(jsonPath("$.email").value(email));
        }

        @Test
        @DisplayName("should return 401 Unauthorized when user is not authenticated")
        void should_return401Unauthorized_when_userIsNotAuthenticated() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/{username}", "someuser"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "testuser", roles = "USER")
        @DisplayName("should return 403 Forbidden when user accesses another user's profile")
        void should_return403Forbidden_when_userIsNotAuthorized() throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/{username}", "anotheruser"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "admin", roles = "ADMIN")
        @DisplayName("should return 404 Not Found when user does not exist")
        void should_return404NotFound_when_userIsNotFound() throws Exception {
            // Arrange
            String nonExistentUser = "nonexistent";
            when(userService.getUserByUsername(nonExistentUser))
                .thenThrow(new UserNotFoundException("User not found"));

            // Act & Assert
            mockMvc.perform(get("/api/users/{username}", nonExistentUser))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @ValueSource(strings = {"a", "some-random-username-which-is-very-long"})
        @WithMockUser(username = "admin", roles = "ADMIN")
        @DisplayName("should return 400 Bad Request for invalid username format")
        void should_return400BadRequest_when_usernameIsInvalid(String invalidUsername) throws Exception {
            // Act & Assert
            mockMvc.perform(get("/api/users/{username}", invalidUsername))
                    .andExpect(status().isBadRequest());
        }

    }
    
}
