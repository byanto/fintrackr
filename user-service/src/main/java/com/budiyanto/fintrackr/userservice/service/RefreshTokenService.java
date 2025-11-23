package com.budiyanto.fintrackr.userservice.service;

import java.util.List;

import com.budiyanto.fintrackr.userservice.entity.RefreshToken;
import com.budiyanto.fintrackr.userservice.entity.User;

public interface RefreshTokenService {

    String createToken(User user);

    List<RefreshToken> findByUser(User user);

    RefreshToken verifyExpiration(RefreshToken token);

    String rotateToken(RefreshToken oldToken);

    void invalidateAllTokensForUser(User user);

}
