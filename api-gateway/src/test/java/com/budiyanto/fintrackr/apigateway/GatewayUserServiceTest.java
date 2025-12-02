package com.budiyanto.fintrackr.apigateway;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.budiyanto.fintrackr.apigateway.service.JwtService;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// @AutoConfigureWireMock(port = 0)
@ActiveProfiles("test")
public class GatewayUserServiceTest {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private Clock clock;

    // WireMock Server 1: for user-service
    @RegisterExtension
    static WireMockExtension userServiceMockServer = WireMockExtension.newInstance()
            .options(WireMockConfiguration.wireMockConfig().dynamicPort())
            .build();

    @DynamicPropertySource
    static void configureServiceDiscovery(DynamicPropertyRegistry registry) {
        // Configure User Service
        registry.add("spring.cloud.discovery.client.simple.instances.user-service[0].uri", 
            () -> "http://localhost:" + userServiceMockServer.getPort());
    }

    @Test
    @DisplayName("Should return OK when request to public endpoint")
    void should_returnOk_when_requestToPublicEndpoint() {
        // Mock the downstream service for the public endpoint
        userServiceMockServer.stubFor(post(urlEqualTo("/api/auth/login"))
                .willReturn(aResponse().withStatus(200)));

        webClient.post().uri("/api/auth/login")
                // Even though the mock doesn't require it, sending a dummy body is good practice
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("Should return Unauthorized when request to secured endpoint without token")
    void should_returnUnauthorized_when_requestToSecuredEndpointWithoutToken() {
        webClient.get().uri("/api/users/me")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Should return Unauthorized when request to secured endpoint with invalid token")
    void should_returnUnauthorized_when_requestToSecuredEndpointWithInvalidToken() {
        String invalidToken = "invalid-token";

        webClient.get().uri("/api/users/me")
                .header("Authorization", "Bearer " + invalidToken)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody();
    }

    @Test
    @WithMockUser(username = "testuser", roles = {"USER"})
    @DisplayName("Should return OK and forwards request when request to secured endpoint with valid token")
    void should_returnOkAndForwardsRequest_when_requestToSecuredEndpointWithValidToken() {
        // 1. Setup WireMock for downstream service (User Service)
        userServiceMockServer.stubFor(get(urlEqualTo("/api/users/me"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"username\":\"testuser\"}")));

        when(clock.instant()).thenReturn(Instant.now());
        String validToken = jwtService.generateAccessToken("testuser", List.of("ROLE_USER"));

        // Perform the request
        webClient.get().uri("/api/users/me")
                .header("Authorization", "Bearer " + validToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.username").isEqualTo("testuser");
    }

}
