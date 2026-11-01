package com.budiyanto.fintrackr.brokerage.application.exception;

import com.budiyanto.fintrackr.shared.BrokerAccountId;

public class BrokerAccountNotFoundException extends RuntimeException {
    public BrokerAccountNotFoundException(BrokerAccountId brokerAccountId) {
        super("Broker account not found. ID: " + brokerAccountId.value());
    }
}
