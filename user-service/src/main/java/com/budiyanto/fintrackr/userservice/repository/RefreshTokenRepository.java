package com.budiyanto.fintrackr.userservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.userservice.entity.RefreshToken;
import com.budiyanto.fintrackr.userservice.entity.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    List<RefreshToken> findByUser(User user);

    int deleteByUser(User user);

}
