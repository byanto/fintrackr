package com.budiyanto.fintrackr.investmentservice.dto;

import java.time.Instant;

import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;

public record InstrumentResponse(
    Long id,
    InstrumentType instrumentType,
    String code,
    String name,
    String currency,
    Instant createdAt
) {}
