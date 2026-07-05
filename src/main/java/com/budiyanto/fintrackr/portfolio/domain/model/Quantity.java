package com.budiyanto.fintrackr.portfolio.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Quantity {

    private final BigDecimal value;

    private Quantity(BigDecimal value) {
        Objects.requireNonNull(value, "Quantity value cannot be null");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity value cannot be negative");
        }
        this.value = value;
    }

    public static Quantity ofShares(BigDecimal value) {
        BigDecimal normalizedValue = normalize(value);
        if (normalizedValue.scale() != 0) {
            throw new IllegalArgumentException("Quantity of shares must be a whole number");
        }
        return new Quantity(normalizedValue);
    }

    public static Quantity ofUnits(BigDecimal value) {
        BigDecimal normalizedValue = normalize(value);
        if (normalizedValue.scale() > 4) {
            throw new IllegalArgumentException("Quantity of units cannot have more than 4 decimal places");
        }
        return new Quantity(value.setScale(4, RoundingMode.UNNECESSARY));
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public BigDecimal value() { return value; }

    // Strip tailing zeros to normalize (e.g., 10.00 -> 10)
    private static BigDecimal normalize(BigDecimal value) {
        Objects.requireNonNull(value, "Quantity value must not be null");
        BigDecimal normalizedValue = value.stripTrailingZeros();
        if (normalizedValue.scale() <= 0) {
            normalizedValue = normalizedValue.setScale(0, RoundingMode.UNNECESSARY);
        }
        return normalizedValue;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Quantity quantity = (Quantity) o;
        return Objects.equals(value, quantity.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
