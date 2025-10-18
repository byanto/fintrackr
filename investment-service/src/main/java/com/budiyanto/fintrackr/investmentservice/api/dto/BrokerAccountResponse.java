package com.budiyanto.fintrackr.investmentservice.api.dto;

import java.time.Instant;

public record BrokerAccountResponse(
    Long id,
    String name,
    String brokerName,
    Instant createdAt
) {}
