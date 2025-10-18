package com.budiyanto.fintrackr.investmentservice.app.exception;

import lombok.Getter;

@Getter
public class HoldingNotFoundException extends RuntimeException {

    private final Long id;

    public HoldingNotFoundException(Long id) {
        super("Holding with ID " + id + " not found.");
        this.id = id;
    }
}
