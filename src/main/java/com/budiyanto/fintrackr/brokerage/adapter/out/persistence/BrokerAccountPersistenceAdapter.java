package com.budiyanto.fintrackr.brokerage.adapter.out.persistence;

import com.budiyanto.fintrackr.brokerage.application.port.out.BrokerAccountRepository;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
class BrokerAccountPersistenceAdapter implements BrokerAccountRepository {

    private final BrokerAccountJpaRepository repository;
    private final BrokerAccountPersistenceMapper mapper;

    @Override
    public Optional<BrokerAccount> findById(BrokerAccountId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public BrokerAccount save(BrokerAccount brokerAccount) {
        repository.save(mapper.toEntity(brokerAccount));
        return brokerAccount;
    }
}
