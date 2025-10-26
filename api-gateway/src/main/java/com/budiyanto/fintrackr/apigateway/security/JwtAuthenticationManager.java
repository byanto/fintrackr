package com.budiyanto.fintrackr.apigateway.security;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;

@Component
@AllArgsConstructor
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        String username;
        try {
            username = jwtUtil.getUsernameFromToken(authToken);
        } catch (Exception ex) {
            return Mono.empty(); // Token is invalid
        }

        if (username != null && jwtUtil.validateToken(authToken)) {
            Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
            List<String> roles = claims.get("roles", List.class);
            List<SimpleGrantedAuthority> authorities = roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
            return Mono.just(new UsernamePasswordAuthenticationToken(username, null, authorities));
        }
        return Mono.empty();
    }
}
