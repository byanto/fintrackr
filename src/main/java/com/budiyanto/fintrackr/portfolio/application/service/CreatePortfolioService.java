package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.brokerage.BrokerageApi;
import com.budiyanto.fintrackr.portfolio.application.exception.UnknownBrokerAccountException;
import com.budiyanto.fintrackr.portfolio.application.port.in.CreatePortfolioCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.CreatePortfolioUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class CreatePortfolioService implements CreatePortfolioUseCase {

    private final PortfolioRepository repository;
    private final BrokerageApi brokerageApi;

    @Override
    @Transactional
    public PortfolioId handle(CreatePortfolioCommand command) {
        if (!brokerageApi.exists(command.brokerAccountId())) {
            throw new UnknownBrokerAccountException(command.brokerAccountId());
        }

        Portfolio portfolio = Portfolio.create(command.brokerAccountId(), command.name());
        repository.save(portfolio);
        return portfolio.id();
    }
}
