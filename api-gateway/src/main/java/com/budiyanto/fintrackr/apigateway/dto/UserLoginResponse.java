package com.budiyanto.fintrackr.apigateway.dto;

import java.util.List;

public record UserLoginResponse(
    String username, 
    List<String> roles
) {}
