package com.budiyanto.fintrackr.userservice.controller;

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
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenRequest;
import com.budiyanto.fintrackr.userservice.dto.AuthenticationTokenResponse;
import com.budiyanto.fintrackr.userservice.dto.UserLoginRequest;
import com.budiyanto.fintrackr.userservice.dto.UserLoginResponse;
import com.budiyanto.fintrackr.userservice.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.userservice.dto.UserResponse;
import com.budiyanto.fintrackr.userservice.exception.InvalidRefreshTokenException;
import com.budiyanto.fintrackr.userservice.exception.UserAlreadyExistsException;
import com.budiyanto.fintrackr.userservice.security.JwtService;
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

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

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
        void should_return201Created_when_registrationSuccess() throws Exception {
            // Arrange
            UserRegistrationRequest request = new UserRegistrationRequest(USERNAME, PASSWORD, EMAIL);
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
        void should_return400BadRequest_when_requestIsInvalid(UserRegistrationRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<UserRegistrationRequest> provideInvalidRegisterRequests() {
            return Stream.of(
                new UserRegistrationRequest("   ", PASSWORD, EMAIL),
                new UserRegistrationRequest(USERNAME, "   ", EMAIL),
                new UserRegistrationRequest(USERNAME, PASSWORD, "   "),
                new UserRegistrationRequest("ab", PASSWORD, EMAIL),
                new UserRegistrationRequest("a very very long username that exceeds the limit", PASSWORD, EMAIL),
                new UserRegistrationRequest(USERNAME, "abc", EMAIL),
                new UserRegistrationRequest(USERNAME, "a very very long password that exceeds the limit", EMAIL),
                new UserRegistrationRequest(USERNAME, PASSWORD, "invalidemailformat")
            );
        }

        @Test
        @DisplayName("should return 409 Conflict when username is taken")
        void should_return409Conflict_when_usernameIsTaken() throws Exception {
            // Arrange
            UserRegistrationRequest request = new UserRegistrationRequest(USERNAME, PASSWORD, EMAIL);
            when(authenticationService.registerUser(request)).thenThrow(new UserAlreadyExistsException(USERNAME));

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("should return 200 OK on successful login")
        void should_return200Ok_when_loginSuccess() throws Exception {
            // Arrange
            UserLoginRequest request = new UserLoginRequest(USERNAME, PASSWORD);
            String accessToken = "access.token";
            String refreshToken = "refresh.token";
            UserLoginResponse response = new UserLoginResponse(USER_ID, USERNAME, EMAIL, List.of(ROLE_USER), accessToken, refreshToken);
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
                    .andExpect(jsonPath("$.roles[0]").value(ROLE_USER))
                    .andExpect(jsonPath("$.accessToken").value(accessToken))
                    .andExpect(jsonPath("$.refreshToken").value(refreshToken));
            
            // Verify that the service method was called with the correct argument
            verify(authenticationService).authenticate(request);
        }

        @Test
        @DisplayName("should return 401 Unauthorized for bad credentials")
        void should_return401Unauthorized_when_badCredentials() throws Exception {
            // Arrange
            UserLoginRequest request = new UserLoginRequest(USERNAME, "wrongpassword");
            when(authenticationService.authenticate(request)).thenThrow(new BadCredentialsException("Invalid username or password"));

            // Act & Assert
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isUnauthorized());                    
        }

        @ParameterizedTest
        @MethodSource("provideInvalidLoginRequests")
        @DisplayName("should return 400 BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(UserLoginRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<UserLoginRequest> provideInvalidLoginRequests() {
            return Stream.of(
                new UserLoginRequest("   ", PASSWORD),
                new UserLoginRequest(USERNAME, "   "),
                new UserLoginRequest("ab", PASSWORD),
                new UserLoginRequest("a very very long username that exceeds the limit", PASSWORD),
                new UserLoginRequest(USERNAME, "abc"),
                new UserLoginRequest(USERNAME, "a very very long password that exceeds the limit")
            );
        }
    }

    @Nested
    @DisplayName("POST /api/auth/renewtoken")
    class RenewAuthTokenEndpoint {

        @Test
        @DisplayName("should return 200 OK with new tokens on valid refresh token")
        void should_return200Ok_when_refreshTokenIsValid() throws Exception {
            // Arrange
            String oldRefreshTokenValue = UUID.randomUUID().toString();
            AuthenticationTokenRequest request = new AuthenticationTokenRequest(USERNAME, oldRefreshTokenValue);
            
            String newRefreshTokenValue = UUID.randomUUID().toString();
            String newAccessTokenValue = "new.access.token";
            long expiresIn = 3600000L;

            AuthenticationTokenResponse response = new AuthenticationTokenResponse(newAccessTokenValue, newRefreshTokenValue, expiresIn);
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
        void should_return403Forbidden_when_tokenIsNotFound() throws Exception {
            // Arrange
            String nonExistentToken = UUID.randomUUID().toString();
            AuthenticationTokenRequest request = new AuthenticationTokenRequest(USERNAME, nonExistentToken);
            when(authenticationService.renewAuthToken(request)).thenThrow(new InvalidRefreshTokenException("Refresh Token %s not found".formatted(nonExistentToken)));

            // Act & Assert
            mockMvc.perform(post("/api/auth/renewtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("should return 403 Forbidden when token is expired")
        void should_return403Forbidden_when_tokenIsExpired() throws Exception {
            // Arrange
            String expiredToken = UUID.randomUUID().toString();
            AuthenticationTokenRequest request = new AuthenticationTokenRequest(USERNAME, expiredToken);
            when(authenticationService.renewAuthToken(request)).thenThrow(new InvalidRefreshTokenException("Refresh Token %s has expired".formatted(expiredToken)));

            // Act & Assert
            mockMvc.perform(post("/api/auth/renewtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidAuthTokenRequests")
        @DisplayName("should return 400 BadRequest when any field is invalid")
        void should_return400BadRequest_when_requestIsInvalid(AuthenticationTokenRequest invalidRequest) throws Exception {            
            // Act & Assert
            mockMvc.perform(post("/api/auth/renewtoken")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        private static Stream<AuthenticationTokenRequest> provideInvalidAuthTokenRequests() {
            String refreshTokenValue = UUID.randomUUID().toString();
            return Stream.of(
                new AuthenticationTokenRequest("   ", refreshTokenValue),
                new AuthenticationTokenRequest(USERNAME, "   "),
                new AuthenticationTokenRequest("ab", refreshTokenValue),
                new AuthenticationTokenRequest("a very very long username that exceeds the limit", refreshTokenValue)
            );
        }
    }
}
