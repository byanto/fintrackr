package com.budiyanto.fintrackr.apigateway.service;

import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {

    @Value("${fintrackr.jwt.secret}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.secret));
    }

    public Claims getAllClaimsFromAccessToken(final String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T getClaimFromAccessToken(Claims claims, String token, Function<Claims, T> claimsResolver) {        
        return claimsResolver.apply(claims);
    }

    public String getUserIdFromAccessToken(Claims claims, String token) {
        return getClaimFromAccessToken(claims, token, Claims::getId);
    }

    public String getUsernameFromAccessToken(Claims claims, String token) {
        return getClaimFromAccessToken(claims, token, Claims::getSubject);
    }

    public boolean isValidToken(String token) {
        // The parser will throw an exception if the token is invalid (e.g., expired, malformed, wrong signature)
        getAllClaimsFromAccessToken(token);
        return true;
    }
    
}
