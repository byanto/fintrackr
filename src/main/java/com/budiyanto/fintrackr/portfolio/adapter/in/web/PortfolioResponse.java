package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import java.math.BigDecimal;
import java.util.UUID;

record PortfolioResponse(
    UUID id,
    UUID brokerAccountId,
    String name,
    BigDecimal tradingBalance
) { }
