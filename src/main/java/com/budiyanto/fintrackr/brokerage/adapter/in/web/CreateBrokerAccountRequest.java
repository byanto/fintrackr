package com.budiyanto.fintrackr.brokerage.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

record CreateBrokerAccountRequest(
    @NotBlank String name,
    @NotNull BigDecimal buyRate,
    @NotNull BigDecimal sellRate
) { }
