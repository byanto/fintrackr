package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.brokerage.BrokerageApi;
import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordDepositCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordDepositUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.Deposit;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import java.time.Clock;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class RecordDepositService implements RecordDepositUseCase {

    private final PortfolioRepository portfolioRepository;
    private final BrokerageApi brokerageApi;
    private final Clock clock;

    @Override
    @Transactional
    public TransactionId handle(RecordDepositCommand command) {
        Portfolio portfolio = portfolioRepository.findById(command.portfolioId())
                .orElseThrow(() -> new PortfolioNotFoundException(command.portfolioId()));
        Deposit deposit = portfolio.recordDeposit(command.amount(), command.date(), LocalDate.now(clock));
        portfolioRepository.save(portfolio);

        brokerageApi.applyCashFlow(portfolio.brokerAccountId(), deposit.cashFlow());
        return deposit.id();
    }
}
