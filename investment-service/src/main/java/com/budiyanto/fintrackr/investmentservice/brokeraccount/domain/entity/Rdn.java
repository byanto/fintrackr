package com.budiyanto.fintrackr.investmentservice.brokeraccount.domain.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.money.MonetaryAmount;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Rdn {

    private final String bankName;
    private final String accountNumber;
    private final MonetaryAmount balance;

    public static Rdn create(String bankName, String accountNumber, MonetaryAmount balance) {
        return new Rdn(bankName, accountNumber, balance);
    }

}