package com.budiyanto.fintrackr.portfolio.application.exception;

import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;

public class PortfolioNotFoundException extends  RuntimeException {
    public PortfolioNotFoundException(PortfolioId portfolioId) {
        super("Portfolio not found. ID: " + portfolioId);
    }
}
