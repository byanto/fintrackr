package com.budiyanto.fintrackr.portfolio.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.portfolio.application.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.portfolio.application.exception.TransactionNotFoundException;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("GetTransactionService Tests")
class GetTransactionServiceTest {

    private final InMemoryPortfolioRepository inMemoryPortfolioRepository = new InMemoryPortfolioRepository();
    private final GetTransactionService getTransactionService = new GetTransactionService(inMemoryPortfolioRepository);

    private static final BigDecimal BALANCE = new BigDecimal("1000000");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 15);

    @Test
    @DisplayName("Return transaction when it belongs to portfolio")
    void should_returnTransaction_when_itBelongsToPortfolio() {
        // Given
        Portfolio portfolio = savedPortfolio();
        Transaction transaction = portfolio.transactions().get(1);

        // When
        Transaction result = getTransactionService.handle(portfolio.id(), transaction.id());

        // Then
        assertThat(result).isEqualTo(transaction);
    }

    @Test
    @DisplayName("Throw transaction not found when transaction belongs to another portfolio")
    void should_throwTransactionNotFound_when_transactionBelongsToAnotherPortfolio() {
        // Given
        Portfolio portfolio1 = savedPortfolio();
        Portfolio portfolio2 = savedPortfolio();
        Transaction transactionPortfolio2 = portfolio2.transactions().getFirst();

        // When & Then
        assertThatThrownBy(() -> getTransactionService.handle(portfolio1.id(), transactionPortfolio2.id()))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessageContaining("Transaction not found in portfolio");
    }

    @Test
    @DisplayName("Throw portfolio not found when portfolio does not exist")
    void should_throwPortfolioNotFound_when_portfolioDoesNotExist() {
        // When & Then
        assertThatThrownBy(() -> getTransactionService.handle(PortfolioId.generate(), TransactionId.generate()))
                .isInstanceOf(PortfolioNotFoundException.class)
                .hasMessageContaining("Portfolio not found");
    }

    private Portfolio savedPortfolio() {
        Portfolio portfolio = Portfolio.create(BrokerAccountId.generate(), "Test Portfolios");
        portfolio.recordDeposit(Money.of(BALANCE), TODAY.minusDays(10), TODAY);
        portfolio.recordBuy(
                AssetId.of("ID1000109507"),
                Quantity.ofShares(new BigDecimal("100")),
                Money.of(new BigDecimal("2000")),
                Money.of(new BigDecimal("3500")),
                TODAY.minusDays(5),
                TODAY
        );
        portfolio.recordDeposit(Money.of(BALANCE.multiply(BigDecimal.TWO)), TODAY.minusDays(2), TODAY);
        inMemoryPortfolioRepository.save(portfolio);
        return portfolio;
    }

}
