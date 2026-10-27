package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

record RecordBuyRequest(
        @NotBlank String isin,
        @NotNull @Positive BigDecimal quantity,
        @NotBlank String quantityKind,
        @NotNull @Positive BigDecimal price,
        BigDecimal fee, // null → fee is computed from the broker's FeeStructure
        @NotNull LocalDate date
) { }
