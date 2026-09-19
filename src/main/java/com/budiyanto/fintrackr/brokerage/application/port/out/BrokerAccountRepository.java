package com.budiyanto.fintrackr.brokerage.application.port.out;

import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import java.util.Optional;

public interface BrokerAccountRepository {

    Optional<BrokerAccount> findById(BrokerAccountId id);

    BrokerAccount save(BrokerAccount brokerAccount);
}
