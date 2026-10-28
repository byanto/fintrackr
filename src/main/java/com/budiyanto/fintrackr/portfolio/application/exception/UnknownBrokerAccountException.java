package com.budiyanto.fintrackr.portfolio.application.exception;

import com.budiyanto.fintrackr.shared.BrokerAccountId;

public class UnknownBrokerAccountException extends RuntimeException {
    public UnknownBrokerAccountException(BrokerAccountId brokerAccountId) {
        super("Broker account does not exist. ID: " + brokerAccountId.value());
    }
}
