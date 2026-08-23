package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryPortfolioRepository implements PortfolioRepository {

    private final Map<PortfolioId, Portfolio> portfolios = new HashMap<>();

    @Override
    public Optional<Portfolio> findById(PortfolioId id) {
        return Optional.ofNullable(portfolios.get(id));
    }

    @Override
    public Portfolio save(Portfolio portfolio) {
        portfolios.put(portfolio.id(), portfolio);
        return portfolio;
    }

    void clear() {
        portfolios.clear();
    }

}
