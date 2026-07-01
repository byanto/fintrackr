package com.budiyanto.fintrackr.portfolio.domain.exception;

import com.budiyanto.fintrackr.shared.DomainException;
import com.budiyanto.fintrackr.shared.Money;

public class NonPositiveAmountException extends DomainException {

    public NonPositiveAmountException(Money amount) {
        super("Non positive amount is not allowed: " + amount.amount());
    }

}
