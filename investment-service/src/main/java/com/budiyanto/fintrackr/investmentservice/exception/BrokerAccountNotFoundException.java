package com.budiyanto.fintrackr.investmentservice.exception;

import lombok.Getter;

@Getter
public class BrokerAccountNotFoundException extends RuntimeException {

    private final Long id;
    public BrokerAccountNotFoundException(Long id) {
        super("BrokerAccount with ID " + id + " not found.");
        this.id = id;
    }
}
