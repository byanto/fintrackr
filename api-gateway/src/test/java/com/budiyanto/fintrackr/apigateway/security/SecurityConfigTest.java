package com.budiyanto.fintrackr.apigateway.security;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToIgnoreCase;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.budiyanto.fintrackr.apigateway.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWireMock(port = 0, stubs = "classpath:/stubs")
class SecurityConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void should_returnOk_when_requestToPublicEndpoint() {
        // Mock the downstream service for the public endpoint
        stubFor(post(urlEqualTo("/api/auth/login"))
                .withHeader("Content-Type", containing("application/json"))
                .willReturn(aResponse().withStatus(200)));

        webTestClient.post().uri("/api/auth/login")
                // Even though the mock doesn't require it, sending a dummy body is good practice
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void should_returnUnauthorized_when_requestToSecuredEndpointWithoutToken() {
        webTestClient.get().uri("/api/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void should_returnUnauthorized_when_requestToSecuredEndpointWithInvalidToken() {
        String invalidToken = "invalid-token";
        when(jwtService.isValidToken(invalidToken)).thenReturn(false);

        webTestClient.get().uri("/api/users/me")
                .header("Authorization", "Bearer " + invalidToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void should_returnOkAndForwardsRequest_when_requestToSecuredEndpointWithValidToken() {
        // Mock the downstream service
        stubFor(get(urlEqualTo("/api/users/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"username\":\"testuser\"}")));

        // Mock the JWT validation
        String validToken = "valid-token-for-test";
        Claims claims = Jwts.claims()
                                .subject("testuser")
                                .add("roles", List.of("ROLE_USER"))
                                .build();

        when(jwtService.isValidToken(validToken)).thenReturn(true);
        when(jwtService.getAllClaimsFromAccessToken(validToken)).thenReturn(claims);

        // Perform the request
        webTestClient.get().uri("/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("testuser");
    }

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("user-service.uri", () -> "http://localhost:${wiremock.server.port}");
        registry.add("investment-service.uri", () -> "http://localhost:${wiremock.server.port}");
    }
}
