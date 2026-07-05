package com.budiyanto.fintrackr.portfolio.domain.exception;

import com.budiyanto.fintrackr.shared.DomainException;
import com.budiyanto.fintrackr.shared.Money;

public class NegativeFeeException extends DomainException {

    public NegativeFeeException(Money amount) {
        super("Negative fee is not allowed: " + amount.amount());
    }

}
