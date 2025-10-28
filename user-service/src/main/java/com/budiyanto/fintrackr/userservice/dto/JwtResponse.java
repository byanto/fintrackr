package com.budiyanto.fintrackr.userservice.dto;

import java.util.List;

public record JwtResponse(
    String accessToken,
    String refreshToken,
    Long id,
    String username,
    List<String> roles) {
}