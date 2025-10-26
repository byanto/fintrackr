package com.budiyanto.fintrackr.investmentservice.exception;

import lombok.Getter;

@Getter
public class FeeRuleNotFoundException extends RuntimeException {
    private final Long id;
    public FeeRuleNotFoundException(Long id) {
        super("Fee Rule with id " + id + " not found");
        this.id = id;
    }
}
