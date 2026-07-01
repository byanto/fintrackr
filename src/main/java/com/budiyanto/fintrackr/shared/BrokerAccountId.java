package com.budiyanto.fintrackr.shared;

import org.springframework.validation.ObjectError;

import java.util.Objects;
import java.util.UUID;

public record BrokerAccountId(UUID value) {

    public BrokerAccountId {
        Objects.requireNonNull(value, "value cannot be null");
    }

    public static BrokerAccountId generate() {
        return new BrokerAccountId(UUID.randomUUID());
    }

}
