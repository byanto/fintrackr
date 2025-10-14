package com.budiyanto.fintrackr.investmentservice.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.budiyanto.fintrackr.investmentservice.domain.TradeType;

public record TradeResponse(
    Long id,
    Long portfolioId,
    Long instrumentId,
    TradeType tradeType,
    BigDecimal quantity,
    BigDecimal price,
    Instant tradedAt
) {}
