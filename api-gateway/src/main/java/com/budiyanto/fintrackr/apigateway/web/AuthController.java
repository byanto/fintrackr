package com.budiyanto.fintrackr.apigateway.web;

import org.springframework.http.MediaType;
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
import com.budiyanto.fintrackr.apigateway.security.JwtUtil;

import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final JwtUtil jwtUtil;
    private final WebClient webClient;

    public AuthController(JwtUtil jwtUtil, WebClient.Builder webClientBuilder) {
        this.jwtUtil = jwtUtil;
        this.webClient = webClientBuilder.baseUrl("http://user-service").build();
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        return webClient.post()
                .uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(authRequest)
                .retrieve()
                .bodyToMono(UserLoginResponse.class)
                .map(userLoginResponse -> {
                    String token = jwtUtil.generateToken(userLoginResponse.username(), userLoginResponse.roles());
                    return ResponseEntity.ok(new AuthResponse(token));
                })
                .onErrorResume(WebClientResponseException.class, ex ->
                        Mono.just(ResponseEntity.status(ex.getStatusCode()).build()));
    }
}
