package com.budiyanto.fintrackr.brokerage.application.service;

import com.budiyanto.fintrackr.brokerage.application.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.brokerage.application.port.in.BrokerAccountDetails;
import com.budiyanto.fintrackr.brokerage.application.port.in.GetBrokerAccountUseCase;
import com.budiyanto.fintrackr.brokerage.application.port.out.BrokerAccountRepository;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class GetBrokerAccountService implements GetBrokerAccountUseCase {

    private final BrokerAccountRepository repository;

    @Override
    @Transactional(readOnly = true)
    public BrokerAccountDetails handle(BrokerAccountId brokerAccountId) {
        return repository.findById(brokerAccountId).map(account ->
            new BrokerAccountDetails(
                    account.id().value(),
                    account.name(),
                    account.rdn().amount(),
                    account.feeStructure().buyRate().rate(),
                    account.feeStructure().sellRate().rate()
            )
        ).orElseThrow(() -> new BrokerAccountNotFoundException(brokerAccountId));
    }
}
