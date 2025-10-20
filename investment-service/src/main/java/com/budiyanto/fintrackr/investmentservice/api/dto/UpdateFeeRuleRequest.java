package com.budiyanto.fintrackr.investmentservice.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record UpdateFeeRuleRequest(
        @NotNull(message = "Fee percentage cannot be null")
        @PositiveOrZero(message = "Fee percentage must be positive or zero")
        BigDecimal feePercentage,

        @NotNull(message = "Minimum fee cannot be null")
        @PositiveOrZero(message = "Minimum fee must be positive or zero")
        BigDecimal minFee
) {}
