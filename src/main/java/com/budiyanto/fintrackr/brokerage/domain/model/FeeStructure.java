package com.budiyanto.fintrackr.brokerage.domain.model;

import java.util.Objects;

public record FeeStructure(Percentage buyRate, Percentage sellRate) {

    public FeeStructure {
        Objects.requireNonNull(buyRate, "buyRate cannot be null");
        Objects.requireNonNull(sellRate, "sellRate cannot be null");
    }

    public static FeeStructure of(Percentage buyRate, Percentage sellRate) {
        return new FeeStructure(buyRate, sellRate);
    }

}
