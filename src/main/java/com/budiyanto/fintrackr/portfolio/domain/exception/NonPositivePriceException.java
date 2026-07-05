package com.budiyanto.fintrackr.portfolio.domain.exception;

import com.budiyanto.fintrackr.shared.DomainException;
import com.budiyanto.fintrackr.shared.Money;

public class NonPositivePriceException extends DomainException {

    public NonPositivePriceException(Money amount) {
        super("Non positive price is not allowed: " + amount.amount());
    }

}
