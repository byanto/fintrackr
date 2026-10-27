package com.budiyanto.fintrackr.brokerage.application.service;

import com.budiyanto.fintrackr.brokerage.application.port.in.CreateBrokerAccountCommand;
import com.budiyanto.fintrackr.brokerage.application.port.in.CreateBrokerAccountUseCase;
import com.budiyanto.fintrackr.brokerage.application.port.out.BrokerAccountRepository;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CreateBrokerAccountService implements CreateBrokerAccountUseCase {

    private final BrokerAccountRepository repository;

    @Override
    @Transactional
    public BrokerAccountId handle(CreateBrokerAccountCommand command) {
        BrokerAccount brokerAccount = repository.save(BrokerAccount.create(
                command.name(),
                command.feeStructure()
        ));
        return brokerAccount.id();
    }
}
