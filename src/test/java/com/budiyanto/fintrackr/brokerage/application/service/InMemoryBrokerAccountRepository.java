package com.budiyanto.fintrackr.brokerage.application.service;

import com.budiyanto.fintrackr.brokerage.application.port.out.BrokerAccountRepository;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryBrokerAccountRepository implements BrokerAccountRepository {

    private final Map<BrokerAccountId, BrokerAccount> brokerAccounts = new HashMap<>();

    @Override
    public Optional<BrokerAccount> findById(BrokerAccountId id) {
        BrokerAccount foundBrokerAccount = brokerAccounts.get(id);
        if (foundBrokerAccount == null) {
            return Optional.empty();
        } else {
            return Optional.of(BrokerAccount.reconstitute(
                    foundBrokerAccount.id(), foundBrokerAccount.name(), foundBrokerAccount.rdn(), foundBrokerAccount.feeStructure()));
        }
    }

    @Override
    public BrokerAccount save(BrokerAccount brokerAccount) {
        brokerAccounts.put(brokerAccount.id(), brokerAccount);
        return brokerAccount;
    }

}
