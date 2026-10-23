package com.budiyanto.fintrackr.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public class Quantity {

    private final BigDecimal value;

    private Quantity(BigDecimal value) {
        checkNotNull(value);
        this.value = value;
    }

    public static Quantity ofShares(BigDecimal value) {
        checkNotNull(value);
        checkNotNegative(value);
        BigDecimal normalizedValue = normalize(value);
        if (normalizedValue.scale() != 0) {
            throw new IllegalArgumentException("Quantity of shares must be a whole number");
        }
        return new Quantity(normalizedValue);
    }

    public static Quantity ofUnits(BigDecimal value) {
        checkNotNull(value);
        checkNotNegative(value);
        BigDecimal normalizedValue = normalize(value);
        if (normalizedValue.scale() > 4) {
            throw new IllegalArgumentException("Quantity of units cannot have more than 4 decimal places");
        }
        return new Quantity(value.setScale(4, RoundingMode.UNNECESSARY));
    }

    // Load path: skips creation rules, the value was validated when it was recorded
    public static Quantity reconstitute(BigDecimal value) {
        return new Quantity(value);
    }

    public boolean isZero() {
        return value.compareTo(BigDecimal.ZERO) == 0;
    }

    public BigDecimal value() { return value; }

    private static void checkNotNull(BigDecimal value) {
        Objects.requireNonNull(value, "Quantity value cannot be null");
    }

    // Strip trailing zeros to normalize (e.g., 10.00 -> 10)
    private static BigDecimal normalize(BigDecimal value) {
        BigDecimal normalizedValue = value.stripTrailingZeros();
        if (normalizedValue.scale() <= 0) {
            normalizedValue = normalizedValue.setScale(0, RoundingMode.UNNECESSARY);
        }
        return normalizedValue;
    }

    private static void checkNotNegative(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Quantity value cannot be negative");
        }
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
