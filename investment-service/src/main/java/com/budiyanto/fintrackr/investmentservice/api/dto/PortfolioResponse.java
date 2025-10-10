package com.budiyanto.fintrackr.investmentservice.api.dto;

import java.time.Instant;

public record PortfolioResponse(
    Long id, 
    String name, 
    Instant createdAt
) {}
