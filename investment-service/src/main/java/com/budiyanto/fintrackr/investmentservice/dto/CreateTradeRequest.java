package com.budiyanto.fintrackr.investmentservice.dto;

import java.math.BigDecimal;
import java.time.Instant;

import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateTradeRequest(
    @NotNull(message = "Portfolio id cannot be null")
    Long portfolioId,

    @NotNull(message = "Instrument id cannot be null")
    Long instrumentId,

    @NotNull(message = "Trade type cannot be null")
    TradeType tradeType,
    
    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    BigDecimal quantity,

    @NotNull(message = "Price cannot be null")
    @Positive(message = "Price must be positive")
    BigDecimal price,

    @NotNull(message = "Traded time cannot be null")
    Instant tradedAt
) {}
