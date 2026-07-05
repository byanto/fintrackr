package com.budiyanto.fintrackr.portfolio.domain.model;

import java.util.Objects;
import java.util.UUID;

public record AcquisitionId(UUID value) {

    public AcquisitionId {
        Objects.requireNonNull(value, "value cannot be null");
    }

    public static AcquisitionId generate() {
        return new AcquisitionId(UUID.randomUUID());
    }

}
