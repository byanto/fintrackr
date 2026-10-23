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
import org.junit.jupiter.params.provider.MethodSource;

class BuyTest {

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
        TransactionId id = TransactionId.generate();
        PortfolioId portfolioId = PortfolioId.generate();
        LocalDate date = LocalDate.now();
        AssetId assetId = AssetId.of("ID1000109507");
        Quantity quantity = Quantity.ofShares(new BigDecimal("1000"));
        Money price = Money.of(new BigDecimal("10000"));
        Money fee = Money.of(new BigDecimal("1500"));
        AcquisitionId acquisitionId = AcquisitionId.generate();

        return Stream.of(
                Arguments.of(null, portfolioId, date, assetId, quantity, price, fee, acquisitionId, "id cannot be null"),
                Arguments.of(id, null, date, assetId, quantity, price, fee, acquisitionId, "portfolioId cannot be null"),
                Arguments.of(id, portfolioId, null, assetId, quantity, price, fee, acquisitionId, "date cannot be null"),
                Arguments.of(id, portfolioId, date, null, quantity, price, fee, acquisitionId, "assetId cannot be null"),
                Arguments.of(id, portfolioId, date, assetId, null, price, fee, acquisitionId, "quantity cannot be null"),
                Arguments.of(id, portfolioId, date, assetId, quantity, null, fee, acquisitionId, "price cannot be null"),
                Arguments.of(id, portfolioId, date, assetId, quantity, price, null, acquisitionId, "fee cannot be null"),
                Arguments.of(id, portfolioId, date, assetId, quantity, price, fee, null, "acquisitionId cannot be null")
        );
    }

    @Test
    @DisplayName("Keep the given id when reconstitute")
    void should_keepGivenId_when_reconstitute() {
        // Given
        TransactionId id = TransactionId.generate();
        PortfolioId portfolioId = PortfolioId.generate();
        LocalDate date = LocalDate.now();
        AssetId assetId = AssetId.of("ID1000109507");
        Quantity quantity = Quantity.ofShares(new BigDecimal("1000"));
        Money price = Money.of(new BigDecimal("10000"));
        Money fee = Money.of(new BigDecimal("1500"));
        AcquisitionId acquisitionId = AcquisitionId.generate();

        // When
        Buy result = Buy.reconstitute(id, portfolioId, date, assetId, quantity, price, fee, acquisitionId);

        // Then
        assertThat(result.id()).isEqualTo(id);
    }
}
