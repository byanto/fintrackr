package com.budiyanto.fintrackr.investmentservice.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record HoldingResponse(
    Long id,
    Long portfolioId,
    Long instrumentId,
    BigDecimal quantity,
    BigDecimal averagePrice,
    Instant updatedAt
) {}
