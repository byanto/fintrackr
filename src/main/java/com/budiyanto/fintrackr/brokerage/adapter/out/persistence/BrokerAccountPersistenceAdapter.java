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
        Optional<BrokerAccountJpaEntity> entityOptional = repository.findById(brokerAccount.id().value());
        BrokerAccountJpaEntity entity;
        if (entityOptional.isPresent()) {
            entity = entityOptional.get();
            mapper.updateEntity(brokerAccount, entity);
        } else {
            entity = mapper.toEntity(brokerAccount);
        }
        repository.save(entity);
        return brokerAccount;
    }
}
