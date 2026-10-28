package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
class PortfolioPersistenceAdapter implements PortfolioRepository {

    private final PortfolioJpaRepository repository;
    private final PortfolioPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Portfolio> findById(PortfolioId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Portfolio save(Portfolio portfolio) {
        Optional<PortfolioJpaEntity> entityOptional = repository.findById(portfolio.id().value());
        PortfolioJpaEntity entity;
        if (entityOptional.isPresent()) {
            entity = entityOptional.get();
            mapper.updateEntity(portfolio, entity);
        } else {
            entity = mapper.toEntity(portfolio);
            repository.save(entity);
        }

        return portfolio;
    }

    @Override
    public boolean existsById(PortfolioId id) {
        return repository.existsById(id.value());
    }
}
