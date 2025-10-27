package com.budiyanto.fintrackr.userservice.service;

import com.budiyanto.fintrackr.userservice.domain.RefreshToken;
import com.budiyanto.fintrackr.userservice.domain.User;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenExpiredException;
import com.budiyanto.fintrackr.userservice.exception.UserNotFoundException;
import com.budiyanto.fintrackr.userservice.repository.RefreshTokenRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    @Value("${jwt.refreshtoken.expiration.ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        RefreshToken refreshToken = new RefreshToken(
            user,
            UUID.randomUUID().toString(),
            Instant.now().plusMillis(refreshTokenDurationMs)
        );

        return refreshTokenRepository.save(refreshToken);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException(token.getToken());
        }
        return token;
    }
}
