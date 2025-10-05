package com.budiyanto.fintrackr.investmentservice.app.exception;

import lombok.Getter;

@Getter
public class PortfolioNotFoundException extends RuntimeException {

    private final Long portfolioId;

    public PortfolioNotFoundException(Long portfolioId) {
        super("Portfolio with ID " + portfolioId + " not found.");
        this.portfolioId = portfolioId;
    }

}
