package com.budiyanto.fintrackr.userservice.dto;

import java.util.List;

public record LoginResponse(
        String username,
        List<String> roles
) {}
