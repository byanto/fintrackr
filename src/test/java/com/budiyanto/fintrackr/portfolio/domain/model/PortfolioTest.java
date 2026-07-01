package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.portfolio.domain.exception.FutureDatedTransactionException;
import com.budiyanto.fintrackr.portfolio.domain.exception.NonPositiveAmountException;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.DomainException;
import com.budiyanto.fintrackr.shared.Money;

import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Portfolio Tests")
class PortfolioTest {

    private Portfolio portfolio;
    private LocalDate date;
    private LocalDate today;

    @BeforeEach
    void setup() {
        portfolio = Portfolio.create(BrokerAccountId.generate(), "Long-Term");
        date = LocalDate.of(2026, 6, 20);
        today = LocalDate.of(2026, 6, 28);
    }

    @Nested
    @DisplayName("RecordDeposit Tests")
    class RecordDeposit {

        // Happy Path
        @Test
        @DisplayName("Increase the trading balance when a single deposit is correctly recorded")
        void should_increaseTradingBalance_when_recordDeposit() {
            // Given
            Money amount = Money.of(new BigDecimal("1000"));

            // When
            portfolio.recordDeposit(amount, date, today);

            // Then
            assertThat(portfolio.tradingBalance()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Accumulate the trading balance when multiple deposits are correctly recorded")
        void should_accumulateTradingBalance_when_recordMultipleDeposits() {
            // Given
            Money amount1 = Money.of(new BigDecimal(1500));
            Money amount2 = Money.of(new BigDecimal(2000));
            Money amount3 = Money.of(new BigDecimal(3500));

            // When
            portfolio.recordDeposit(amount1, date, today);
            portfolio.recordDeposit(amount2, date, today);
            portfolio.recordDeposit(amount3, date, today);

            // Then
            assertThat(portfolio.tradingBalance()).isEqualTo(Money.of(new BigDecimal(7000)));
        }

        @Test
        @DisplayName("Allow deposit when date is today")
        void should_allowDeposit_when_dateIsToday() {
            // Given
            Money amount = Money.of(new BigDecimal("15000"));

            // When
            portfolio.recordDeposit(amount, today, today);

            // Then
            assertThat(portfolio.tradingBalance()).isEqualTo(amount);
        }

        @Test
        @DisplayName("Record a Deposit transaction when a deposit is correctly recorded")
        @Disabled("not yet implemented")
        void should_recordDepositTransaction_when_recordDeposit() {

        }

        // Invariant Violations
        @ParameterizedTest
        @ValueSource(strings = {"0", "-1500"})
        @DisplayName("Reject a deposit when the amount is zero or negative")
        void should_throwException_when_amountLessThanOrEqualZero(String input) {
            // Given
            Money amount = Money.of(new BigDecimal(input));

            // When & Then
            assertThatThrownBy(() -> portfolio.recordDeposit(amount, date, today))
                    .isInstanceOf(NonPositiveAmountException.class);
        }

        @Test
        @DisplayName("Reject a deposit when the date is in the future")
        void should_throwException_when_dateInFuture() {
            // Given
            Money amount = Money.of(new BigDecimal("1000"));
            LocalDate futureDate = LocalDate.of(2026, 12, 15);

            // When & Then
            assertThatThrownBy(() -> portfolio.recordDeposit(amount, futureDate, today))
                    .isInstanceOf(FutureDatedTransactionException.class);
        }

        @ParameterizedTest
        @CsvSource({
                "-15000, 2026-06-01",
                "20000, 2026-12-15"
        })
        @DisplayName("Keep balance unchanged when deposit is rejected")
        void should_leaveBalanceUnchanged_when_depositRejected(String amount, LocalDate depositDate) {
            // Given
            Money firstDeposit = Money.of(new BigDecimal("10000"));
            Money secondDeposit = Money.of(new BigDecimal(amount));
            LocalDate validDate = LocalDate.of(2026, 6, 1);

            portfolio.recordDeposit(firstDeposit, validDate, today);
            Money balanceBefore = portfolio.tradingBalance();

            // When
            assertThatThrownBy(() -> portfolio.recordDeposit(secondDeposit, depositDate, today))
                    .isInstanceOf(DomainException.class);

            // Then
            assertThat(portfolio.tradingBalance()).isEqualTo(balanceBefore);
        }


    }

}
