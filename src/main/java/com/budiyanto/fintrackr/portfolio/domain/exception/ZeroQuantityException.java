package com.budiyanto.fintrackr.portfolio.domain.exception;

import com.budiyanto.fintrackr.shared.DomainException;
import com.budiyanto.fintrackr.shared.Quantity;

public class ZeroQuantityException extends DomainException {

    public ZeroQuantityException(Quantity quantity) {
        super("Zero Quantity is not allowed: " + quantity.value());
    }
}
