package com.budiyanto.fintrackr.userservice.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.userservice.entity.RefreshToken;
import com.budiyanto.fintrackr.userservice.entity.User;
import com.budiyanto.fintrackr.userservice.exception.RefreshTokenExpiredException;
import com.budiyanto.fintrackr.userservice.exception.UserNotFoundException;
import com.budiyanto.fintrackr.userservice.repository.RefreshTokenRepository;
import com.budiyanto.fintrackr.userservice.repository.UserRepository;
import com.budiyanto.fintrackr.userservice.service.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Value("${fintrackr.jwt.refreshtoken.expiration.ms}")
    private Long refreshTokenDurationMs;

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public String createToken(User user) {
        String username = user.getUsername();
        userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));

        String rawToken = UUID.randomUUID().toString();
        String encodedToken = passwordEncoder.encode(rawToken);

        RefreshToken refreshToken = new RefreshToken(
            user,
            encodedToken, // Only save hashed token into the database
            Instant.now().plusMillis(refreshTokenDurationMs)
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken; // Return the raw token to the caller
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> findByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    @Override
    @Transactional
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RefreshTokenExpiredException(token.getValue());
        }
        return token;
    }
    
    @Override
    @Transactional
    public String rotateToken(RefreshToken oldToken) {
        // Invalidate the old token
        refreshTokenRepository.delete(oldToken);
        
        // Create and save a new one for the same user
        return createToken(oldToken.getUser());
    }

    @Override
    @Transactional
    public void invalidateAllTokensForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
