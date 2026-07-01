package com.budiyanto.fintrackr.portfolio.domain.model;

import java.util.Objects;
import java.util.UUID;

public record PortfolioId(UUID value) {

    public PortfolioId {
        Objects.requireNonNull(value, "value cannot be null");
    }

    public static PortfolioId generate() {
        return new PortfolioId(UUID.randomUUID());
    }

}
