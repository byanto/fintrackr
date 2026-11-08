package com.budiyanto.fintrackr.portfolio.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.domain.model.Deposit;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("ListTransactionsService Tests")
class ListTransactionsServiceTest {

    private final InMemoryPortfolioRepository inMemoryPortfolioRepository = new InMemoryPortfolioRepository();
    private final ListTransactionsService listTransactionsService = new ListTransactionsService(inMemoryPortfolioRepository);

    private static final BigDecimal BALANCE = new BigDecimal("1000000");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    @Test
    @DisplayName("Return the ledger of the portfolio when it exists")
    void should_returnLedgerOfPortfolio_when_portfolioExists() {
        // Given
        Portfolio portfolio = savedPortfolio();
        TransactionId transactionId1 = portfolio.transactions().get(0).id();
        TransactionId transactionId2 = portfolio.transactions().get(1).id();

        // When
        List<Transaction> transactionList = listTransactionsService.handle(portfolio.id());

        // Then
        assertThat(transactionList).containsExactly(
                Deposit.reconstitute(transactionId1, portfolio.id(), TODAY.minusDays(10), Money.of(BALANCE)),
                Deposit.reconstitute(transactionId2, portfolio.id(), TODAY.minusDays(2), Money.of(BALANCE.multiply(BigDecimal.TWO)))
        );
    }

    @Test
    @DisplayName("Throw portfolio not found when portfolio does not exist")
    void should_throwPortfolioNotFound_when_portfolioDoesNotExist() {
        // Given
        PortfolioId portfolioId = PortfolioId.generate();

        // When & Then
        assertThatThrownBy(() -> listTransactionsService.handle(portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio not found");
    }

    private Portfolio savedPortfolio() {
        Portfolio portfolio = Portfolio.create(BrokerAccountId.generate(), "Test Portfolios");
        portfolio.recordDeposit(Money.of(BALANCE), TODAY.minusDays(10), TODAY);
        portfolio.recordDeposit(Money.of(BALANCE.multiply(BigDecimal.TWO)), TODAY.minusDays(2), TODAY);
        inMemoryPortfolioRepository.save(portfolio);
        return portfolio;
    }

}
