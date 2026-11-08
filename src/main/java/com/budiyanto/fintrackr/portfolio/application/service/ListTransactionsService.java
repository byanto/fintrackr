package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.ListTransactionsUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class ListTransactionsService implements ListTransactionsUseCase {

    private final PortfolioRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<Transaction> handle(PortfolioId portfolioId) {
        Portfolio portfolio = repository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioId));
        return portfolio.transactions();
    }

}
