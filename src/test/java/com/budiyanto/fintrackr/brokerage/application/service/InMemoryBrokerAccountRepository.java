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
        return Optional.ofNullable(brokerAccounts.get(id))
                .map(account -> BrokerAccount.reconstitute(
                    account.id(),
                    account.name(),
                    account.rdn(),
                    account.feeStructure()
                ));
    }

    @Override
    public BrokerAccount save(BrokerAccount brokerAccount) {
        brokerAccounts.put(brokerAccount.id(), brokerAccount);
        return brokerAccount;
    }

    @Override
    public boolean existsById(BrokerAccountId id) {
        return brokerAccounts.containsKey(id);
    }

}
