package com.budiyanto.fintrackr.userservice.dto;

import java.util.List;

public record UserLoginResponse(
        Long id,
        String username,
        String email,
        List<String> roles,
        String accessToken,
        String refreshToken,
        String tokenType
) {
        public UserLoginResponse(Long id, String username, String email, List<String> roles, String accessToken, String refreshToken) {
                this(id, username, email, roles, accessToken, refreshToken, "Bearer");
        }
}