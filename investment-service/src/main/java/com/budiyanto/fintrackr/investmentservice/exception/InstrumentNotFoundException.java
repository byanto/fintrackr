package com.budiyanto.fintrackr.investmentservice.exception;

import lombok.Getter;

@Getter
public class InstrumentNotFoundException extends RuntimeException {

    private final Long id;

    public InstrumentNotFoundException(Long id) {
        super("Instrument with ID " + id + " not found.");
        this.id = id;
    }

}
