package com.budiyanto.fintrackr.apigateway.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.budiyanto.fintrackr.apigateway.dto.AuthRequest;
import com.budiyanto.fintrackr.apigateway.dto.UserLoginResponse;
import com.budiyanto.fintrackr.apigateway.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.apigateway.dto.UserResponse;
import com.budiyanto.fintrackr.apigateway.security.JwtAuthenticationManager;
import com.budiyanto.fintrackr.apigateway.security.JwtUtil;
import com.budiyanto.fintrackr.apigateway.security.SecurityConfig;
import com.budiyanto.fintrackr.apigateway.security.SecurityContextRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;

@WebFluxTest(controllers = AuthController.class)
@Import({AuthControllerTest.TestConfig.class, SecurityConfig.class})
@DisplayName("AuthController Tests")
class AuthControllerTest {

    private static MockWebServer mockBackEnd;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private SecurityContextRepository securityContextRepository;

    @MockitoBean
    private JwtAuthenticationManager authenticationManager;

    private static final String USERNAME = "testuser";
    private static final String EMAIL = "test@email.com";
    private static final String PASSWORD = "password123";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String ROLE_USER = "ROLE_USER";

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void setupMocks() {
        // The security filter chain calls this method even for public endpoints.
        // We must mock it to return an empty Mono to avoid a NullPointerException.
        when(securityContextRepository.load(any(ServerWebExchange.class))).thenReturn(Mono.empty());
    }

    @Nested
    @DisplayName("POST /api/auth/login")
    class LoginEndpoint {

        @Test
        @DisplayName("should return 200 OK with token on successful login")
        void should_return200_when_loginSuccess() throws JsonProcessingException {
            // Arrange
            AuthRequest authRequest = new AuthRequest(USERNAME, PASSWORD);
            UserLoginResponse userLoginResponse = new UserLoginResponse(USERNAME, List.of(ROLE_USER));
            String mockToken = "mock.jwt.token";

            mockBackEnd.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(userLoginResponse))
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            when(jwtUtil.generateToken(USERNAME, List.of(ROLE_USER))).thenReturn(mockToken);

            // Act & Assert
            webTestClient.post().uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(authRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").isEqualTo(mockToken);

            // Verify interactions
            verify(jwtUtil).generateToken(USERNAME, List.of(ROLE_USER));
        }

        @Test
        @DisplayName("should return 401 Unauthorized on failed login")
        void should_return401_when_loginFails() {
            // Arrange
            AuthRequest authRequest = new AuthRequest(USERNAME, "wrongpassword");

            mockBackEnd.enqueue(new MockResponse().setResponseCode(401));

            // Act & Assert
            webTestClient.post().uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(authRequest)
                    .exchange()
                    .expectStatus().isUnauthorized();

            // Verify no further interactions occured
            verify(jwtUtil, never()).generateToken(anyString(), anyList());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidLoginRequests")
        @DisplayName("should return 400 BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(AuthRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            webTestClient.post().uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidRequest)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        private static Stream<AuthRequest> provideInvalidLoginRequests() {
            return Stream.of(
                new AuthRequest("   ", PASSWORD),
                new AuthRequest(USERNAME, "   ")
            );
        }
    }

    @Nested
    @DisplayName("POST /api/auth/register")
    class RegisterEndpoint {

        @Test
        @DisplayName("should return 201 Created on successful registration")
        void should_return201_when_registrationSuccess() throws JsonProcessingException {
            // Arrange
            UserRegistrationRequest registerRequest = new UserRegistrationRequest(USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, EMAIL);
            UserResponse userResponse = new UserResponse(1L, USERNAME, EMAIL, Instant.now());

            mockBackEnd.enqueue(new MockResponse()
                    .setResponseCode(201)
                    .setBody(objectMapper.writeValueAsString(userResponse))
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            // Act & Assert
            webTestClient.post().uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(registerRequest)
                    .exchange()
                    .expectStatus().isCreated();
        }

        @ParameterizedTest
        @MethodSource("provideInvalidRegisterRequests")
        @DisplayName("should return 400 BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(UserRegistrationRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            webTestClient.post().uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(invalidRequest)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        private static Stream<UserRegistrationRequest> provideInvalidRegisterRequests() {
            return Stream.of(
                new UserRegistrationRequest("   ", PASSWORD, FIRST_NAME, LAST_NAME, EMAIL),
                new UserRegistrationRequest(USERNAME, "   ", FIRST_NAME, LAST_NAME, EMAIL),
                new UserRegistrationRequest(USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, "   "),
                new UserRegistrationRequest("ab", PASSWORD, FIRST_NAME, LAST_NAME, EMAIL),
                new UserRegistrationRequest("a very very long username that exceeds the limit", PASSWORD, FIRST_NAME, LAST_NAME, EMAIL),
                new UserRegistrationRequest(USERNAME, "abc", FIRST_NAME, LAST_NAME, EMAIL),
                new UserRegistrationRequest(USERNAME, "a very very long password that exceeds the limit", FIRST_NAME, LAST_NAME, EMAIL),
                new UserRegistrationRequest(USERNAME, PASSWORD, "a", LAST_NAME, EMAIL),
                new UserRegistrationRequest(USERNAME, PASSWORD, "a very very very long first name that exceeds the limit", LAST_NAME, EMAIL),
                new UserRegistrationRequest(USERNAME, PASSWORD, FIRST_NAME, "a", EMAIL),
                new UserRegistrationRequest(USERNAME, PASSWORD, FIRST_NAME, "a very very very long last name that exceeds the limit", EMAIL),                
                new UserRegistrationRequest(USERNAME, PASSWORD, FIRST_NAME, LAST_NAME, "invalidemailformat")
            );
        }

        @Test
        @DisplayName("should return 409 Conflict when username is taken")
        void should_return409_when_usernameIsTaken() throws JsonProcessingException {
            // Arrange
            UserRegistrationRequest registerRequest = new UserRegistrationRequest("existinguser", PASSWORD, FIRST_NAME, LAST_NAME, EMAIL);
            String errorBody = "{\"error\":\"Username 'existinguser' is already taken.\"}";

            mockBackEnd.enqueue(new MockResponse()
                    .setResponseCode(409)
                    .setBody(errorBody)
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            // Act & Assert
            webTestClient.post().uri("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(registerRequest)
                    .exchange()
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody()
                    .jsonPath("$.error").isEqualTo("Username 'existinguser' is already taken.");
        }
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WebClient.Builder webClientBuilder() {
            // Create a mock builder
            WebClient.Builder mockBuilder = mock(WebClient.Builder.class);

            // Create a real WebClient that points to our mock server
            String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
            WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();

            // Configure the mock's fluent API chain
            when(mockBuilder.baseUrl(anyString())).thenReturn(mockBuilder);
            when(mockBuilder.build()).thenReturn(webClient);
            return mockBuilder;
        }
    }
}
