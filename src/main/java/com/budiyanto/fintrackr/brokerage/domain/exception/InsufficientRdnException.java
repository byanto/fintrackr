package com.budiyanto.fintrackr.brokerage.domain.exception;

import com.budiyanto.fintrackr.shared.DomainException;
import com.budiyanto.fintrackr.shared.Money;

public class InsufficientRdnException extends DomainException {

    public InsufficientRdnException(Money rdn, Money delta) {
        super("Apply cash flow rejected: would drive RDN below zero. RDN: " + rdn.amount() + ", Delta: " + delta.amount());
    }

}
