package com.budiyanto.fintrackr.portfolio.domain.exception;

import com.budiyanto.fintrackr.portfolio.domain.model.Quantity;
import com.budiyanto.fintrackr.shared.DomainException;

public class ZeroQuantityException extends DomainException {

    public ZeroQuantityException(Quantity quantity) {
        super("Zero Quantity is not allowed: " + quantity.value());
    }
}
