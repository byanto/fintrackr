package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;

record RecordDepositRequest(
    @NotNull
    @Positive
    @Digits(integer = 19, fraction = 0)
    BigDecimal amount,

    @NotNull LocalDate date
) { }
