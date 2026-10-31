package com.budiyanto.fintrackr.brokerage;

import com.budiyanto.fintrackr.shared.BrokerAccountId;

public class NoSuchBrokerAccountException extends RuntimeException {
    public NoSuchBrokerAccountException(BrokerAccountId id) {
        super("No broker account with id '" + id.value() + "'");
    }
}
