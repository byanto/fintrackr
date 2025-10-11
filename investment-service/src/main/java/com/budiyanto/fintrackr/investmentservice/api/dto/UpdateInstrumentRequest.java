package com.budiyanto.fintrackr.investmentservice.api.dto;

public record UpdateInstrumentRequest(
    String code, 
    String name
) {}
