package com.budiyanto.fintrackr.investmentservice.dto;

import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateInstrumentRequest(
    @NotNull(message = "Instrument type cannot be null")
    InstrumentType instrumentType,

    @NotBlank(message = "Instrument code cannot be blank")
    String code,
    
    @NotBlank(message = "Instrument name cannot be blank")
    String name,
    
    @NotBlank(message = "Instrument currency cannot be blank")
    String currency
) {}
