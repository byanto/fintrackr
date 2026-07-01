package com.budiyanto.fintrackr.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;


public record Money(BigDecimal amount, Currency currency) implements Comparable<Money> {

    public Money {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(currency, "currency cannot be null");
        amount = amount.setScale(0, RoundingMode.HALF_EVEN);
    }

    public static Money of(BigDecimal amount) {
        return new Money(amount, Currency.getInstance("IDR"));
    }

    public static Money zero() {
        return new Money(BigDecimal.ZERO, Currency.getInstance("IDR"));
    }

    public Money add(Money toAdd) {
        checkSameCurrency(toAdd);
        return new Money(amount.add(toAdd.amount), currency);
    }

    @Override
    public int compareTo(Money value) {
        checkSameCurrency(value);
        return amount.compareTo(value.amount);
    }

    public boolean isPositive() {
        return compareTo(Money.zero()) > 0;
    }

    public boolean isNegative() {
        return compareTo(Money.zero()) < 0;
    }

    public boolean isZeroOrNegative() {
        return compareTo(Money.zero()) <= 0;
    }

    private void checkSameCurrency(Money value) {
        Objects.requireNonNull(value, "value cannot be null");
        if (!currency.equals(value.currency)) {
            throw new IllegalArgumentException("The input currency is not equal to currency");
        }
    }
}
