package com.budiyanto.fintrackr.apigateway.dto;

public record AuthRequest(
    String username,
    String password
) {}
