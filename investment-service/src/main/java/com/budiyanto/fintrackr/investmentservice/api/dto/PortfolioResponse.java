package com.budiyanto.fintrackr.investmentservice.api.dto;

import java.time.Instant;

public record PortfolioResponse(
    Long id, 
    String name,
    BrokerAccountInPortfolioResponse brokerAccount,
    Instant createdAt
) {
    public record BrokerAccountInPortfolioResponse(
        Long id,
        String name,
        String brokerName
    ) {}
}
