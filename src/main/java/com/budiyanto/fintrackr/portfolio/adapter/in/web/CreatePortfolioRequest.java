package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

record CreatePortfolioRequest(
    @NotNull UUID brokerAccountId,
    @NotBlank String name
) { }
