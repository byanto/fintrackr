package com.budiyanto.fintrackr.investmentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePortfolioRequest(
    @NotBlank(message = "Portfolio name cannot be blank")
    String name,

    @NotNull(message = "Broker account ID cannot be null")
    Long brokerAccountId
) {}
