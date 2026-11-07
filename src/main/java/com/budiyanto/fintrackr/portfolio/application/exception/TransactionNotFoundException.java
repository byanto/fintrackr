package com.budiyanto.fintrackr.portfolio.application.exception;

import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(PortfolioId portfolioId, TransactionId transactionId) {
        super("Transaction not found in portfolio. Transaction ID: " + transactionId.value() + ", Portfolio ID: " + portfolioId.value());
    }

}
