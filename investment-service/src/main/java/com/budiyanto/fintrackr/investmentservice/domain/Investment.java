package com.budiyanto.fintrackr.investmentservice.domain;

import java.math.BigDecimal;

public record Investment(String id, String name, BigDecimal amount) {}