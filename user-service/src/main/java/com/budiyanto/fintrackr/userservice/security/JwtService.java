package com.budiyanto.fintrackr.userservice.security;

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
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${fintrackr.jwt.secret}")
    private String secret;

    @Value("${fintrackr.jwt.accesstoken.expiration}")
    @Getter
    private Duration accessTokenExpiration;

    private SecretKey key;

    private final Clock clock;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public Claims getAllClaimsFromAccessToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T getClaimFromAccessToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromAccessToken(token);
        return claimsResolver.apply(claims);
    }

    public String getUsernameFromAccessToken(String token) {
        return getClaimFromAccessToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromAccessToken(String token) {
        return getClaimFromAccessToken(token, Claims::getExpiration);
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
