package com.budiyanto.fintrackr.investmentservice.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record HoldingResponse(
    Long id,
    Long portfolioId,
    InstrumentInHoldingResponse instrument,
    BigDecimal quantity,
    BigDecimal averagePrice,
    Instant updatedAt
) {
    public record InstrumentInHoldingResponse(
        Long id, String code, String name, String currency
    ) {}
}
