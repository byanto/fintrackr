package com.budiyanto.fintrackr.apigateway.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.budiyanto.fintrackr.apigateway.dto.AuthRequest;
import com.budiyanto.fintrackr.apigateway.dto.AuthResponse;
import com.budiyanto.fintrackr.apigateway.dto.UserLoginResponse;
import com.budiyanto.fintrackr.apigateway.dto.UserRegistrationRequest;
import com.budiyanto.fintrackr.apigateway.security.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

// @RestController
// @RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final WebClient.Builder webClientBuilder;
    private final JwtUtil jwtUtil;

    // @PostMapping("/register")
    public Mono<ResponseEntity<Object>> register(@Valid @RequestBody UserRegistrationRequest request) {
        return webClientBuilder.baseUrl("http://user-service").build()
                .post()
                .uri("/api/auth/register")
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).build())
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Forward the exact error response from the downstream service
                    return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(ex.getResponseBodyAs(Object.class)));
                });
    }

    // @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        return webClientBuilder.baseUrl("http://user-service").build()
                .post()
                .uri("/api/auth/login")
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(UserLoginResponse.class)
                .map(userLoginResponse -> {
                    String token = jwtUtil.generateToken(userLoginResponse.username(), userLoginResponse.roles());
                    return ResponseEntity.ok(new AuthResponse(token));
                })
                .onErrorResume(WebClientResponseException.class,
                        ex -> Mono.just(ResponseEntity.status(ex.getStatusCode()).build()));
    }
}
