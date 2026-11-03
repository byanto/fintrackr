package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.GetPortfolioUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.in.PortfolioDetails;
import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class GetPortfolioService implements GetPortfolioUseCase {

    private final PortfolioRepository repository;

    @Override
    @Transactional(readOnly = true)
    public PortfolioDetails handle(PortfolioId portfolioId) {
        return repository.findById(portfolioId).map(portfolio ->
            new PortfolioDetails(
                    portfolio.id().value(),
                    portfolio.brokerAccountId().value(),
                    portfolio.name(),
                    portfolio.tradingBalance().amount()
            )
        ).orElseThrow(() -> new PortfolioNotFoundException(portfolioId));
    }
}
