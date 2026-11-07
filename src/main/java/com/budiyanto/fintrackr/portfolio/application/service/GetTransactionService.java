package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.exception.TransactionNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.GetTransactionUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class GetTransactionService implements GetTransactionUseCase {

    private final PortfolioRepository repository;

    @Override
    @Transactional(readOnly = true)
    public Transaction handle(PortfolioId portfolioId, TransactionId transactionId) {
        Portfolio portfolio = repository.findById(portfolioId)
                .orElseThrow(() -> new PortfolioNotFoundException(portfolioId));
        return portfolio.transactions().stream()
                .filter(t -> t.id().equals(transactionId)).findFirst()
                .orElseThrow(() -> new TransactionNotFoundException(portfolioId, transactionId));
    }
}
