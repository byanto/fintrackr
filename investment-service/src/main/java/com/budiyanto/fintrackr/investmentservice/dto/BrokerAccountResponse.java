package com.budiyanto.fintrackr.investmentservice.dto;

import java.time.Instant;

public record BrokerAccountResponse(
    Long id,
    String name,
    String brokerName,
    Instant createdAt
) {}
