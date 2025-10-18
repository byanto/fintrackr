package com.budiyanto.fintrackr.investmentservice.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateBrokerAccountRequest(
    @NotBlank(message = "Account name cannot be blank")
    String name,

    @NotBlank(message = "Broker name cannot be blank")
    String brokerName
) {}
