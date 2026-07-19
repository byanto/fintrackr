package com.budiyanto.fintrackr.brokerage.domain.model;

import com.budiyanto.fintrackr.shared.Quantity;
import com.budiyanto.fintrackr.shared.Money;

import java.math.BigDecimal;
import java.util.Objects;

public record FeeStructure(Percentage buyRate, Percentage sellRate) {

    public FeeStructure {
        Objects.requireNonNull(buyRate, "buyRate cannot be null");
        Objects.requireNonNull(sellRate, "sellRate cannot be null");
    }

    public static FeeStructure of(Percentage buyRate, Percentage sellRate) {
        return new FeeStructure(buyRate, sellRate);
    }

    public Money computeBuyFee(Quantity quantity, Money price) {
        return computeFee(quantity, price, buyRate);
    }

    public Money computeSellFee(Quantity quantity, Money price) {
        return computeFee(quantity, price, sellRate);
    }

    private Money computeFee(Quantity quantity, Money price, Percentage rate) {
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(price, "price cannot be null");

        if (price.isNegative()) {
            throw new IllegalArgumentException("price cannot be negative");
        }

        BigDecimal result = quantity.value().multiply(price.amount()).multiply(rate.rate());
        return Money.of(result, price.currency());
    }

}
