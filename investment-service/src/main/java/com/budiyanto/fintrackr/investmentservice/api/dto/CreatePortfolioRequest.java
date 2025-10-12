package com.budiyanto.fintrackr.investmentservice.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePortfolioRequest(
    @NotBlank(message = "Portfolio name cannot be blank")
    String name
) {}
