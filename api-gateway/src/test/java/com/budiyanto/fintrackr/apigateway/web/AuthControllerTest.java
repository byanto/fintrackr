package com.budiyanto.fintrackr.apigateway.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.budiyanto.fintrackr.apigateway.dto.AuthRequest;
import com.budiyanto.fintrackr.apigateway.dto.UserLoginResponse;
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

    public static MockWebServer mockBackEnd;

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
            AuthRequest authRequest = new AuthRequest("testuser", "password");
            UserLoginResponse userLoginResponse = new UserLoginResponse("testuser", List.of("ROLE_USER"));
            String mockToken = "mock.jwt.token";

            mockBackEnd.enqueue(new MockResponse()
                    .setBody(objectMapper.writeValueAsString(userLoginResponse))
                    .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

            when(jwtUtil.generateToken("testuser", List.of("ROLE_USER"))).thenReturn(mockToken);

            // Act & Assert
            webTestClient.post().uri("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(authRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.token").isEqualTo(mockToken);

            // Verify interactions
            verify(jwtUtil).generateToken("testuser", List.of("ROLE_USER"));
        }

        @Test
        @DisplayName("should return 401 Unauthorized on failed login")
        void should_return401_when_loginFails() {
            // Arrange
            AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");

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
