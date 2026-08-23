package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.brokerage.BrokerageApi;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;

import java.util.HashMap;
import java.util.Map;

public class InMemoryBrokerageApi implements BrokerageApi {

    private final Map<BrokerAccountId, BrokerAccount> brokerAccounts = new HashMap<>();

    @Override
    public Money computeBuyFee(BrokerAccountId accountId, Quantity quantity, Money price) {
        return brokerAccounts.get(accountId).computeBuyFee(quantity, price);
    }

    @Override
    public void applyCashFlow(BrokerAccountId accountId, Money delta) {
        brokerAccounts.get(accountId).applyCashFlow(delta);
    }

    void addBrokerAccount(BrokerAccount brokerAccount) {
        brokerAccounts.put(brokerAccount.id(), brokerAccount);
    }

    void clear() {
        brokerAccounts.clear();
    }
}
