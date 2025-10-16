package com.budiyanto.fintrackr.investmentservice.app.exception;

import java.math.BigDecimal;

import lombok.Getter;

@Getter
public class InsufficientHoldingsException extends RuntimeException {
    private final BigDecimal attemptedQuantity;
    private final BigDecimal availableQuantity;
    private final Long portfolioId;
    private final Long instrumentId;

    public InsufficientHoldingsException(BigDecimal attemptedQuantity, BigDecimal availableQuantity, Long portfolioId, Long instrumentId) {
        super("Cannot sell " + attemptedQuantity + " of instrument " + instrumentId +
              ". Only " + availableQuantity + " is available in portfolio " + portfolioId + ".");
        this.attemptedQuantity = attemptedQuantity;
        this.availableQuantity = availableQuantity;
        this.portfolioId = portfolioId;
        this.instrumentId = instrumentId;
    }
}