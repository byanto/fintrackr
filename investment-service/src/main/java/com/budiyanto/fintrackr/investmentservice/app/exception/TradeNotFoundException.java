package com.budiyanto.fintrackr.investmentservice.app.exception;

import lombok.Getter;

@Getter
public class TradeNotFoundException extends RuntimeException {

    private final Long id;

    public TradeNotFoundException(Long id) {
        super("Tradef with ID " + id + " not found.");
        this.id = id;
    }

}
