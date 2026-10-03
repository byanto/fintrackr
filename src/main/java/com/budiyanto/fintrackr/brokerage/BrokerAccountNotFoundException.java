package com.budiyanto.fintrackr.brokerage;

import com.budiyanto.fintrackr.shared.BrokerAccountId;

public class BrokerAccountNotFoundException extends RuntimeException {
    public BrokerAccountNotFoundException(BrokerAccountId id) {
        super("Broker account with id '" + id.value() + "' not found");
    }
}
