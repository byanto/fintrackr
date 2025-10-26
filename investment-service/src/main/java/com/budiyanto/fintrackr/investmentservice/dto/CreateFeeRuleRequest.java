package com.budiyanto.fintrackr.investmentservice.dto;

import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record CreateFeeRuleRequest(
        @NotNull(message = "Broker account ID cannot be null")
        Long brokerAccountId,
        
        @NotNull(message = "Instrument type cannot be null")
        InstrumentType instrumentType,
        
        @NotNull(message = "Trade type cannot be null")
        TradeType tradeType,

        @NotNull(message = "Fee percentage cannot be null")
        @PositiveOrZero(message = "Fee percentage must be positive or zero")
        @Max(value = 1, message = "Fee percentage must be less than or equal to 1")
        BigDecimal feePercentage,

        @NotNull(message = "Minimum fee cannot be null")
        @PositiveOrZero(message = "Minimum fee must be positive or zero")
        BigDecimal minFee
) {}
