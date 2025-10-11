package com.budiyanto.fintrackr.investmentservice.app.exception;

import lombok.Getter;

@Getter
public class PortfolioNotFoundException extends RuntimeException {

    private final Long id;

    public PortfolioNotFoundException(Long id) {
        super("Portfolio with ID " + id + " not found.");
        this.id = id;
    }

}
