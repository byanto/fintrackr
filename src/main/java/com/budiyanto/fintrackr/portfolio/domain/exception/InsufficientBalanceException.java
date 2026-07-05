package com.budiyanto.fintrackr.portfolio.domain.exception;

import com.budiyanto.fintrackr.shared.DomainException;
import com.budiyanto.fintrackr.shared.Money;

public class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException(Money tradingBalance, Money cost) {
        super("Insufficient balance is not allowed. Balance: " + tradingBalance.amount() + " Cost: " + cost.amount());
    }

}
