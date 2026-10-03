package com.budiyanto.fintrackr.brokerage.application.service;

import com.budiyanto.fintrackr.brokerage.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.brokerage.BrokerageApi;
import com.budiyanto.fintrackr.brokerage.application.port.out.BrokerAccountRepository;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class BrokerageService implements BrokerageApi {

    private final BrokerAccountRepository brokerAccountRepository;

    @Override
    @Transactional(readOnly = true)
    public Money computeBuyFee(BrokerAccountId accountId, Quantity quantity, Money price) {
        BrokerAccount brokerAccount = loadAccount(accountId);
        return brokerAccount.computeBuyFee(quantity, price);
    }

    @Override
    @Transactional
    public void applyCashFlow(BrokerAccountId accountId, Money delta) {
        BrokerAccount brokerAccount = loadAccount(accountId);
        brokerAccount.applyCashFlow(delta);
        brokerAccountRepository.save(brokerAccount);
    }

    private BrokerAccount loadAccount(BrokerAccountId accountId) {
        return brokerAccountRepository.findById(accountId).orElseThrow(
                ()  -> new BrokerAccountNotFoundException(accountId)
        );
    }
}
