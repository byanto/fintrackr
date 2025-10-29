package com.budiyanto.fintrackr.userservice.dto;

import java.time.Instant;

public record UserResponse(
    Long id,
    String username,
    String email,
    Instant createdAt
) {}
