package com.budiyanto.fintrackr.portfolio.application.service;

import com.budiyanto.fintrackr.brokerage.BrokerageApi;
import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyCommand;
import com.budiyanto.fintrackr.portfolio.application.port.in.RecordBuyUseCase;
import com.budiyanto.fintrackr.portfolio.application.port.out.PortfolioRepository;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.shared.Money;
import lombok.RequiredArgsConstructor;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;

@RequiredArgsConstructor
public class RecordBuyService implements RecordBuyUseCase {

    private final PortfolioRepository portfolioRepository;
    private final BrokerageApi brokerageApi;
    private final Clock clock;

    @Override
    public void handle(RecordBuyCommand command) {
        Optional<Portfolio> portfolioOptional = portfolioRepository.findById(command.portfolioId());
        if  (portfolioOptional.isEmpty()) {
            throw new PortfolioNotFoundException(command.portfolioId());
        }

        Portfolio portfolio = portfolioOptional.get();

        Money fee = Optional.ofNullable(command.fee())
                .orElseGet(() -> brokerageApi.computeBuyFee(portfolio.brokerAccountId(), command.quantity(), command.price()));

        LocalDate today = LocalDate.now(clock);
        Money delta = portfolio.recordBuy(command.assetId(), command.quantity(), command.price(), fee, command.date(), today);
        portfolioRepository.save(portfolio);

        brokerageApi.applyCashFlow(portfolio.brokerAccountId(), delta);

    }

}
