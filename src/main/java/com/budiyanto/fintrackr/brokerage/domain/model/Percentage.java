package com.budiyanto.fintrackr.brokerage.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Percentage(BigDecimal rate) {

    public Percentage {
        Objects.requireNonNull(rate, "Percentage rate must not be null");
        if (rate.compareTo(BigDecimal.ZERO) < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Percentage rate must be between 0 and 1, inclusive");
        }
        rate = rate.setScale(6, RoundingMode.HALF_EVEN);
    }

    public static Percentage of(BigDecimal rate) {
        return new Percentage(rate);
    }
}
