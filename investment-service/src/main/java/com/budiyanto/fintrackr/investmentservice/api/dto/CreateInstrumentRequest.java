package com.budiyanto.fintrackr.investmentservice.api.dto;

import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;

public record CreateInstrumentRequest(
    InstrumentType instrumentType, 
    String code, 
    String name, 
    String currency
) {}
