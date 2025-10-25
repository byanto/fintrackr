package com.budiyanto.fintrackr.apigateway.web;

import com.budiyanto.fintrackr.apigateway.dto.AuthRequest;
import com.budiyanto.fintrackr.apigateway.dto.AuthResponse;
import com.budiyanto.fintrackr.apigateway.security.JwtUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Mono<ResponseEntity<AuthResponse>> login(@RequestBody AuthRequest authRequest) {
        // IMPORTANT: This is a hardcoded user for demonstration purposes.
        // In a real application, you would use a service to look up the user
        // from a database and check their hashed password.
        if ("user".equals(authRequest.username()) && "password".equals(authRequest.password())) {
            String token = jwtUtil.generateToken(authRequest.username());
            return Mono.just(ResponseEntity.ok(new AuthResponse(token)));
        } else {
            return Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
        }
    }
}
