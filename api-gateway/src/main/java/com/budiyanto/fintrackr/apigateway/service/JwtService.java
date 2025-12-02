package com.budiyanto.fintrackr.apigateway.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${fintrackr.jwt.secret}")
    private String secret;

    @Value("${fintrackr.jwt.expiration}")
    private Duration accessTokenExpiration;

    private SecretKey key;

    private final Clock clock;

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
        try {
            // The parser will throw an exception if the token is invalid (e.g., expired, malformed, wrong signature)
            getAllClaimsFromAccessToken(token);
            return true;
        } catch(Exception ex) {
            // Any exception (MalformedJwtException, ExpiredJwtException, etc.) means the token is invalid
            return false;
        }    
    }

    public String generateAccessToken(String username, List<String> roles) {
        var claims = new HashMap<String, Object>();
        // Add roles to the token
        claims.put("roles", roles);

        Instant now = clock.instant();
        Date currentDate = Date.from(now);
        Date expDate = Date.from(now.plus(accessTokenExpiration));
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expDate)
                .signWith(key)
                .compact();
    }
    
}
