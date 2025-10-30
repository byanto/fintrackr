package com.budiyanto.fintrackr.userservice.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.userservice.dto.AuthTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.LoginRequest;
import com.budiyanto.fintrackr.userservice.dto.LoginResponse;
import com.budiyanto.fintrackr.userservice.dto.RegisterRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenExpiredException;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenNotFoundException;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.security.SecurityConfig;
import com.budiyanto.fintrackr.userservice.service.AuthenticationService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfig.class)
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthenticationService authenticationService;

    private static final Long USER_ID = 1L;
    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "password123";
    private static final String ROLE_USER = "ROLE_USER";

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("should return 201 Created on successful registration")
        void should_return201_when_registrationSuccess() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest(USERNAME, PASSWORD, EMAIL);
            UserResponse response = new UserResponse(USER_ID, USERNAME, EMAIL, Instant.now());
            when(authenticationService.registerUser(request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(USER_ID))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.email").value(EMAIL));

            // Verify that the service method was called with the correct argument
            verify(authenticationService).registerUser(request);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidRegisterRequests")
        @DisplayName("should return 400 BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(RegisterRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<RegisterRequest> provideInvalidRegisterRequests() {
            return Stream.of(
                new RegisterRequest("   ", PASSWORD, EMAIL),
                new RegisterRequest(USERNAME, "   ", EMAIL),
                new RegisterRequest(USERNAME, PASSWORD, "   "),
                new RegisterRequest("ab", PASSWORD, EMAIL),
                new RegisterRequest("a very long username that exceeds the limit", PASSWORD, EMAIL),
                new RegisterRequest(USERNAME, "abc", EMAIL),
                new RegisterRequest(USERNAME, "a very long password that exceeds the limit", EMAIL)
            );
        }

        @Test
        @DisplayName("should return 409 Conflict when username is taken")
        void should_return409_when_usernameIsTaken() throws Exception {
            // Arrange
            RegisterRequest request = new RegisterRequest(USERNAME, PASSWORD, EMAIL);
            when(authenticationService.registerUser(request)).thenThrow(new UserAlreadyExistsException(USERNAME));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.error").value("Username 'testuser' is already taken."));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("should return 200 OK on successful login")
        void should_return200_when_loginSuccess() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest(USERNAME, PASSWORD);
            LoginResponse response = new LoginResponse(USERNAME, List.of(ROLE_USER));
            when(authenticationService.authenticate(request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.username").value(USERNAME))
                    .andExpect(jsonPath("$.roles.size()").value(1))
                    .andExpect(jsonPath("$.roles[0]").value(ROLE_USER));
            
            // Verify that the service method was called with the correct argument
            verify(authenticationService).authenticate(request);
        }

        @Test
        @DisplayName("should return 401 Unauthorized for bad credentials")
        void should_return401_when_badCredentials() throws Exception {
            // Arrange
            LoginRequest request = new LoginRequest(USERNAME, "wrongpassword");
            when(authenticationService.authenticate(request)).thenThrow(new BadCredentialsException("Invalid username or password"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid username or password"));
        }

        @ParameterizedTest
        @MethodSource("provideInvalidLoginRequests")
        @DisplayName("should return 400 BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(LoginRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<LoginRequest> provideInvalidLoginRequests() {
            return Stream.of(
                new LoginRequest("   ", PASSWORD),
                new LoginRequest(USERNAME, "   ")
            );
        }
    }

    @Nested
    @DisplayName("POST /api/auth/renewtoken")
    class RenewTokenEndpoint {

        @Test
        @DisplayName("should return 200 OK with new tokens on valid refresh token")
        void should_return200_when_refreshTokenIsValid() throws Exception {
            // Arrange
            String oldRefreshTokenValue = UUID.randomUUID().toString();
            AuthTokenRequest request = new AuthTokenRequest(oldRefreshTokenValue);
            
            String newRefreshTokenValue = UUID.randomUUID().toString();
            String newAccessTokenValue = "new.access.token";
            long expiresIn = 3600000L;

            AuthTokenResponse response = new AuthTokenResponse(newAccessTokenValue, newRefreshTokenValue, expiresIn);
            when(authenticationService.renewAuthToken(request)).thenReturn(response);    

            // Act & Assert
            mockMvc.perform(post("/api/auth/renewtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value(newAccessTokenValue))
                    .andExpect(jsonPath("$.refreshToken").value(newRefreshTokenValue))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(expiresIn));

            // Verify interactions
            verify(authenticationService).renewAuthToken(request);            
        }

        @Test
        @DisplayName("should return 403 Forbidden when token is not found")
        void should_returnForbidden_when_tokenIsNotFound() throws Exception {
            // Arrange
            String nonExistentToken = UUID.randomUUID().toString();
            AuthTokenRequest request = new AuthTokenRequest(nonExistentToken);
            when(authenticationService.renewAuthToken(request)).thenThrow(new RefreshTokenNotFoundException(nonExistentToken));

            // Act & Assert
            mockMvc.perform(post("/api/auth/renewtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 Forbidden when token is expired")
        void should_returnForbidden_when_tokenIsExpired() throws Exception {
            // Arrange
            String expiredToken = UUID.randomUUID().toString();
            AuthTokenRequest request = new AuthTokenRequest(expiredToken);
            when(authenticationService.renewAuthToken(request)).thenThrow(new RefreshTokenExpiredException(expiredToken));

            // Act & Assert
            mockMvc.perform(post("/api/auth/renewtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 400 BadRequest when any field is invalid")
        void should_returnBadRequest_when_tokenIsInvalid() throws Exception {
            // Arrange
            AuthTokenRequest request = new AuthTokenRequest("   "); 
                   
            // Act & Assert
            mockMvc.perform(post("/api/auth/renewtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
