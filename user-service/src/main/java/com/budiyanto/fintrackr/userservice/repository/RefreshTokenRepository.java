package com.budiyanto.fintrackr.userservice.repository;

import com.budiyanto.fintrackr.userservice.domain.RefreshToken;
import com.budiyanto.fintrackr.userservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    int deleteByUser(User user);
}
