package com.budiyanto.fintrackr.portfolio.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

public record PortfolioDetails(
    UUID id,
    UUID brokerAccountId,
    String name,
    BigDecimal tradingBalance
) { }
