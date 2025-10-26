package com.budiyanto.fintrackr.userservice.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.userservice.config.SecurityConfig;
import com.budiyanto.fintrackr.userservice.dto.LoginRequest;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;
import com.budiyanto.fintrackr.userservice.dto.RegisterRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class) // Import the actual security configuration
@DisplayName("UserController Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("POST /api/users/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("should return 201 Created on successful registration")
        void should_return201_when_registrationSuccess() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("testuser", "test@email.com", "password123");
            UserResponse response = new UserResponse(1L, "testuser", "test@email.com", Instant.now());
            when(userService.registerUser(any(RegisterRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1L))
                    .andExpect(jsonPath("$.username").value("testuser"));
        }

        @Test
        @DisplayName("should return 409 Conflict when username is taken")
        void should_return409_when_usernameIsTaken() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest("testuser", "test@email.com", "password123");
            when(userService.registerUser(any(RegisterRequest.class))).thenThrow(new UserAlreadyExistsException("testuser"));

            // Act & Assert
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Username 'testuser' is already taken."));
        }

        @Test
        @DisplayName("should return 400 Bad Request for invalid request body")
        void should_return400_when_invalidRequestBody() throws Exception {
            // Arrange: username is blank
            RegisterRequest request = new RegisterRequest("", "test@email.com", "password123");

            // Act & Assert
            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/users/login")
    class LoginEndpoint {

        @Test
        @DisplayName("should return 200 OK on successful login")
        void should_return200_when_loginSuccess() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("testuser", "password123");
            LoginResponse response = new LoginResponse("testuser", List.of("ROLE_USER"));
            when(userService.authenticate(any(LoginRequest.class))).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value("testuser"))
                    .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
        }

        @Test
        @DisplayName("should return 401 Unauthorized for bad credentials")
        void should_return401_when_badCredentials() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest("testuser", "wrongpassword");
            when(userService.authenticate(any(LoginRequest.class))).thenThrow(new BadCredentialsException("Invalid username or password"));

            // Act & Assert
            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid username or password"));
        }

        @Test
        @DisplayName("should return 400 Bad Request for invalid request body")
        void should_return400_when_invalidRequestBody() throws Exception {
            // Arrange: password is blank
            LoginRequest request = new LoginRequest("testuser", "");

            // Act & Assert
            mockMvc.perform(post("/api/users/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isBadRequest());
        }
    }
}