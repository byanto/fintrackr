package com.budiyanto.fintrackr.investmentservice.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateInstrumentRequest(
    @NotBlank(message = "Instrument code cannot be blank")
    String code, 

    @NotBlank(message = "Instrument name cannot be blank")
    String name
) {}
