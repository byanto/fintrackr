package com.budiyanto.fintrackr.portfolio.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class BuyTest {

    private static final TransactionId TRANSACTION_ID = TransactionId.generate();
    private static final PortfolioId PORTFOLIO_ID = PortfolioId.generate();
    private static final LocalDate BUY_DATE = LocalDate.now();
    private static final AssetId ASSET_ID = AssetId.of("ID1000109507");
    private static final Quantity QUANTITY = Quantity.ofShares(new BigDecimal("1000"));
    private static final Money PRICE = Money.of(new BigDecimal("10000"));
    private static final Money FEE = Money.of(new BigDecimal("1500"));
    private static final AcquisitionId ACQUISITION_ID = AcquisitionId.generate();

    @ParameterizedTest(name = "\"{8}\"")
    @MethodSource("nullArgumentProvider")
    @DisplayName("Throw NPE with the field name when a field is null")
    void should_throwNPE_when_fieldIsNull(TransactionId id, PortfolioId portfolioId, LocalDate date, AssetId assetId, Quantity quantity, Money price, Money fee, AcquisitionId acquisitionId, String expectedMessage) {
        // When & Then
        assertThatThrownBy(() -> Buy.reconstitute(id, portfolioId, date, assetId, quantity, price, fee, acquisitionId))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(expectedMessage);
    }

    static Stream<Arguments> nullArgumentProvider() {
        return Stream.of(
                Arguments.of(null, PORTFOLIO_ID, BUY_DATE, ASSET_ID, QUANTITY, PRICE, FEE, ACQUISITION_ID, "id cannot be null"),
                Arguments.of(TRANSACTION_ID, null, BUY_DATE, ASSET_ID, QUANTITY, PRICE, FEE, ACQUISITION_ID, "portfolioId cannot be null"),
                Arguments.of(TRANSACTION_ID, PORTFOLIO_ID, null, ASSET_ID, QUANTITY, PRICE, FEE, ACQUISITION_ID, "date cannot be null"),
                Arguments.of(TRANSACTION_ID, PORTFOLIO_ID, BUY_DATE, null, QUANTITY, PRICE, FEE, ACQUISITION_ID, "assetId cannot be null"),
                Arguments.of(TRANSACTION_ID, PORTFOLIO_ID, BUY_DATE, ASSET_ID, null, PRICE, FEE, ACQUISITION_ID, "quantity cannot be null"),
                Arguments.of(TRANSACTION_ID, PORTFOLIO_ID, BUY_DATE, ASSET_ID, QUANTITY, null, FEE, ACQUISITION_ID, "price cannot be null"),
                Arguments.of(TRANSACTION_ID, PORTFOLIO_ID, BUY_DATE, ASSET_ID, QUANTITY, PRICE, null, ACQUISITION_ID, "fee cannot be null"),
                Arguments.of(TRANSACTION_ID, PORTFOLIO_ID, BUY_DATE, ASSET_ID, QUANTITY, PRICE, FEE, null, "acquisitionId cannot be null")
        );
    }

    @Test
    @DisplayName("Keep the given id when reconstitute")
    void should_keepGivenId_when_reconstitute() {
        // When
        Buy result = Buy.reconstitute(TRANSACTION_ID, PORTFOLIO_ID, BUY_DATE, ASSET_ID, QUANTITY, PRICE, FEE, ACQUISITION_ID);

        // Then
        assertThat(result.id()).isEqualTo(TRANSACTION_ID);
    }

    @Test
    @DisplayName("Reduce cash by quantity times price plus fee when buy")
    void should_reduceCashByQuantityTimesPricePlusFee_when_buy() {
        // Given
        Buy buy = Buy.create(PORTFOLIO_ID, BUY_DATE, ASSET_ID, QUANTITY, PRICE, FEE, ACQUISITION_ID);

        // When
        Money result = buy.cashFlow();

        // Then
        assertThat(result).isEqualTo(Money.of(new BigDecimal("-10001500"))); // - ((1000 * 10000) + 1500) = -10001500
    }

    @ParameterizedTest
    @CsvSource({
            "1.5, 5801, 0, -8702",
            "1.6, 5801, 100, -9382",
            "1.3, 5801, 100, -7641"
    })
    @DisplayName("Round cash flow to whole rupiah when quantity is fractional")
    void should_roundCashFlowToWholeRupiah_when_quantityIsFractional(String inputQuantity, String inputPrice, String inputFee, String expectedCashFlow) {
        // Given
        Quantity quantity = Quantity.ofUnits(new BigDecimal(inputQuantity));
        Money price = Money.of(new BigDecimal(inputPrice));
        Money fee = Money.of(new BigDecimal(inputFee));

        // When
        Buy buy = Buy.create(PORTFOLIO_ID, BUY_DATE, ASSET_ID, quantity, price, fee, ACQUISITION_ID);
        Money result = buy.cashFlow();

        // Then
        assertThat(result).isEqualTo(Money.of(new BigDecimal(expectedCashFlow)));
    }
}
