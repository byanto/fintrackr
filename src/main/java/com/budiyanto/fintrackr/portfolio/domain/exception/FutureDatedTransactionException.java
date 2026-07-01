package com.budiyanto.fintrackr.portfolio.domain.exception;

import com.budiyanto.fintrackr.shared.DomainException;

import java.time.LocalDate;

public class FutureDatedTransactionException extends DomainException {

    public FutureDatedTransactionException(LocalDate date, LocalDate today) {
        super("Future dated transaction is not allowed. Date: " + date + ", Today: " + today + ".");
    }
}
