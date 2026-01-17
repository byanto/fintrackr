package com.budiyanto.fintrackr.investmentservice.brokeraccount.exception;

import lombok.Getter;

@Getter
public class InvalidBrokerAccountNameException extends RuntimeException {

    private final String invalidName;

    public InvalidBrokerAccountNameException(String invalidName) {
        super("Broker account name is invalid: '%s'".formatted(invalidName));
        this.invalidName = invalidName;
    }

}
