package com.budiyanto.fintrackr.brokerage.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record BrokerAccountDetails(
        UUID id,
        String name,
        BigDecimal rdn,
        BigDecimal buyRate,
        BigDecimal sellRate
) {}
