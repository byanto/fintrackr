package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.portfolio.domain.exception.*;
import com.budiyanto.fintrackr.shared.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

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
    class RecordDepositTest {

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
        void should_recordDepositTransaction_when_recordDeposit() {
            // Given
            Money amount = Money.of(new BigDecimal(15000));

            // When
            portfolio.recordDeposit(amount, date, today);

            // Then
            List<Transaction> transactions = portfolio.transactions();
            assertThat(transactions.size()).isEqualTo(1);
            assertThat(transactions.getFirst())
                    .isInstanceOfSatisfying(Deposit.class, d -> {
                        assertThat(d.id()).isNotNull();
                        assertThat(d.portfolioId()).isEqualTo(portfolio.id());
                        assertThat(d.date()).isEqualTo(date);
                        assertThat(d.amount()).isEqualTo(amount);
                    });

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
                "0, 2026-06-01", // Zero amount
                "-15000, 2026-06-01", // Negative amount
                "20000, 2026-12-15" // Future date
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

    @Nested
    @DisplayName("RecordBuy Tests")
    class RecordBuyTest {

        private final AssetId assetId = AssetId.of("ID1000109507"); // BBCA
        private final Quantity quantity = Quantity.ofShares(new BigDecimal("1000"));
        private final Money price = Money.of(new BigDecimal("5800"));
        private final Money fee = Money.of(new BigDecimal("2500"));

        @BeforeEach
        void setup() {
            // Arrange: Ensure the portfolio has sufficient balance for most buy operations.
            // Tests with specific balance requirements will override this by making their own deposits.
            portfolio.recordDeposit(Money.of(new BigDecimal("20000000")), date, today);
        }

        @Test
        @DisplayName("Return the total cost as a negative delta when a buy is recorded")
        void should_returnCostDelta_when_recordBuy() {
            // When
            Money costDelta = portfolio.recordBuy(assetId, quantity, price, fee, date, today);

            // Then
            assertThat(costDelta).isEqualTo(Money.of(new BigDecimal("-5802500"))); // (5800 x 1000) + 2500 = 5802500
        }

        @ParameterizedTest
        @CsvSource({
                "1.5, 5801, 0, -8702",
                "1.6, 5801, 100, -9382",
                "1.3, 5801, 100, -7641"
        })
        @DisplayName("Return the total cost as a negative delta with correct rounding when a buy is recorded")
        void should_returnCorrectRounding_when_recordBuy(String inputQuantity, String inputPrice, String inputFee, String inputCostDelta) {
            // Given
            Quantity quantity = Quantity.ofUnits(new BigDecimal(inputQuantity));
            Money price = Money.of(new BigDecimal(inputPrice));
            Money fee = Money.of(new BigDecimal(inputFee));

            // When
            Money costDelta = portfolio.recordBuy(assetId, quantity, price, fee, date, today);

            // Then
            assertThat(costDelta).isEqualTo(Money.of(new BigDecimal(inputCostDelta)));
        }

        @Test
        @DisplayName("Decrease the trading balance by the total cost when a buy is recorded")
        void should_decreaseBalanceByCost_when_recordBuy() {
            // When
            portfolio.recordBuy(assetId, quantity, price, fee, date, today);

            // Then
            assertThat(portfolio.tradingBalance()).isEqualTo(Money.of(new BigDecimal("14197500"))); // 20000000 - 5802500 = 14197500
        }

        @Test
        @DisplayName("Create a new open acquisition when a buy is recorded")
        void should_createAcquisition_when_recordBuy() {
            // When
            portfolio.recordBuy(assetId, quantity, price, fee, date, today);

            // Then
            var acquisitions = portfolio.acquisitions();
            assertThat(acquisitions).hasSize(1);

            var acquisition = acquisitions.getFirst();
            assertThat(acquisition.id()).isNotNull();
            assertThat(acquisition.portfolioId()).isEqualTo(portfolio.id());
            assertThat(acquisition.assetId()).isEqualTo(assetId);
            assertThat(acquisition.openDate()).isEqualTo(date);
            assertThat(acquisition.openPrice()).isEqualTo(price);
            assertThat(acquisition.openFee()).isEqualTo(fee);
            assertThat(acquisition.initialQuantity()).isEqualTo(quantity);
        }

        @Test
        @DisplayName("Create a second, distinct acquisition when buying the same asset again")
        void should_createSecondAcquisition_when_recordSecondBuyWithSameSymbol() {
            // When
            portfolio.recordBuy(assetId, quantity, price, fee, date, today);
            portfolio.recordBuy(assetId, quantity, price, fee, date, today);

            // Then
            var acquisitions = portfolio.acquisitions();
            var firstAcquisition = acquisitions.getFirst();
            var secondAcquisition = acquisitions.get(1);
            assertThat(acquisitions).hasSize(2);
            assertThat(secondAcquisition.id()).isNotNull();
            assertThat(secondAcquisition.id()).isNotEqualTo(firstAcquisition.id());
            assertThat(portfolio.tradingBalance().amount()).isEqualTo(new BigDecimal("8395000")); // 20000000 - 5802500 - 5802500 =
        }

        @Test
        @DisplayName("Record a Buy transaction in the ledger when a buy is recorded")
        void should_createBuyTransactionAndRecordItInLedger_when_recordBuy() {
            // When
            portfolio.recordBuy(assetId, quantity, price, fee, date, today);

            // Then
            List<Transaction> transactions = portfolio.transactions();
            Acquisition acquisition = portfolio.acquisitions().getFirst();
            assertThat(transactions.size()).isEqualTo(2); // 1 Deposit transaction + 1 Buy transaction
            assertThat(transactions.get(1))
                    .isInstanceOfSatisfying(Buy.class, b -> {
                        assertThat(b.id()).isNotNull();
                        assertThat(b.portfolioId()).isEqualTo(portfolio.id());
                        assertThat(b.date()).isEqualTo(date);
                        assertThat(b.assetId()).isEqualTo(assetId);
                        assertThat(b.quantity()).isEqualTo(quantity);
                        assertThat(b.price()).isEqualTo(price);
                        assertThat(b.fee()).isEqualTo(fee);
                        assertThat(b.acquisitionId()).isEqualTo(acquisition.id());
                    });
        }

        // Boundaries (Edge Cases)
        @Test
        @DisplayName("Allow a buy when the total cost exactly equals the trading balance")
        void should_allowBuy_when_costEqualsBalance() {
            // Given
            portfolio = Portfolio.create(BrokerAccountId.generate(), "Exact Balance Test");
            portfolio.recordDeposit(Money.of(new BigDecimal("5802500")), date, today);

            // When
            portfolio.recordBuy(assetId, quantity, price, fee, date, today);

            // Then
            assertThat(portfolio.tradingBalance()).isEqualTo(Money.zero());
        }

        @Test
        @DisplayName("Allow a buy when the transaction fee is zero")
        void should_allowBuy_when_feeIsZero() {
            // When
            portfolio.recordBuy(assetId, quantity, price, Money.zero(), date, today);

            // Then
            assertThat(portfolio.tradingBalance()).isEqualTo(Money.of(new BigDecimal("14200000"))); // 20000000 - (5800 x 1000 + 0) = 14200000
        }

        // Invariant Violations
        @Test
        @DisplayName("Reject a buy when the quantity is zero")
        void should_throwException_when_quantityEqualZero() {
            // Given
            Quantity zeroQuantity = Quantity.ofShares(new BigDecimal("0"));

            // When & Then
            assertThatThrownBy(() -> portfolio.recordBuy(assetId, zeroQuantity, price, fee, date, today))
                    .isInstanceOf(ZeroQuantityException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"0", "-10000"})
        @DisplayName("Reject a buy when the price is zero or negative")
        void should_throwException_when_priceLessThanOrEqualZero(String input) {
            // Given
            Money invalidPrice = Money.of(new BigDecimal(input));

            // When & Then
            assertThatThrownBy(() -> portfolio.recordBuy(assetId, quantity, invalidPrice, fee, date, today))
                    .isInstanceOf(NonPositivePriceException.class);
        }

        @Test
        @DisplayName("Reject a buy when the fee is negative")
        void should_throwException_when_feeLessThanZero() {
            // Given
            Money negativeFee = Money.of(new BigDecimal("-1000"));

            // When & Then
            assertThatThrownBy(() -> portfolio.recordBuy(assetId, quantity, price, negativeFee, date, today))
                    .isInstanceOf(NegativeFeeException.class);
        }

        @Test
        @DisplayName("Reject a buy when the date is in the future")
        void should_throwException_when_dateInFuture() {
            // Given
            LocalDate futureDate = LocalDate.of(2026, 12, 15);

            // When & Then
            assertThatThrownBy(() -> portfolio.recordBuy(assetId, quantity, price, fee, futureDate, today))
                    .isInstanceOf(FutureDatedTransactionException.class);
        }

        @Test
        @DisplayName("Reject a buy when the total cost exceeds the trading balance")
        void should_throwException_when_costGreaterThanBalance() {
            // Given
            portfolio = Portfolio.create(BrokerAccountId.generate(), "Insufficient Balance Test");
            portfolio.recordDeposit(Money.of(new BigDecimal(4000000)), date, today);

            // When & Then
            assertThatThrownBy(() ->portfolio.recordBuy(assetId, quantity, price, fee, date, today))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @ParameterizedTest
        @CsvSource({
                "0, 10000, 1500, 2026-06-10", // Zero quantity
                "100, 0, 1500, 2026-06-10", // Zero price
                "100, -1000, 1500, 2026-06-10", // Negative price
                "100, 10000, -1500, 2026-06-10", // Negative fee
                "100, 10000, 1500, 2026-12-20", // Future date
                "100, 10000, 1500, 2026-06-10", // cost > balance
        })
        @DisplayName("Keep portfolio state unchanged when a buy is rejected")
        void should_leaveStateUnchanged_when_buyRejected(String inputQuantity, String inputPrice, String inputFee, String inputDate) {
            // Given
            Quantity quantity = Quantity.ofShares(new BigDecimal(inputQuantity));
            Money price = Money.of(new BigDecimal(inputPrice));
            Money fee = Money.of(new BigDecimal(inputFee));
            LocalDate buyingDate = LocalDate.parse(inputDate);

            portfolio = Portfolio.create(BrokerAccountId.generate(), "State Unchanged Test");
            portfolio.recordDeposit(Money.of(new BigDecimal("10000")), LocalDate.of(2026, 6, 1), today);

            Money balanceBefore = portfolio.tradingBalance();

            // When
            assertThatThrownBy(() -> portfolio.recordBuy(assetId, quantity, price, fee, buyingDate, today))
                    .isInstanceOf(DomainException.class);

            // Then
            assertThat(portfolio.tradingBalance()).isEqualTo(balanceBefore);
            assertThat(portfolio.transactions().size()).isEqualTo(1); // Should contain only initial deposit transaction
            assertThat(portfolio.acquisitions().size()).isEqualTo(0); // Should contain no acquisitions
        }

    }

}
