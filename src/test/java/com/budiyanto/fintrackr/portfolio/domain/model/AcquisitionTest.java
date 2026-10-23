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

class AcquisitionTest {

    @ParameterizedTest(name = "\"{7}\"")
    @MethodSource("nullArgumentProvider")
    @DisplayName("Throw NPE with the field name when a field is null")
    void should_throwNPE_when_fieldIsNull(AcquisitionId id, PortfolioId portfolioId, AssetId assetId, LocalDate openDate, Money openPrice, Money openFee, Quantity initialQuantity, String expectedMessage) {
        // When & Then
        assertThatThrownBy(() -> Acquisition.reconstitute(id, portfolioId, assetId, openDate, openPrice, openFee, initialQuantity))
                .isInstanceOf(NullPointerException.class)
                .hasMessage(expectedMessage);
    }

    static Stream<Arguments> nullArgumentProvider() {
        AcquisitionId id = AcquisitionId.generate();
        PortfolioId portfolioId = PortfolioId.generate();
        AssetId assetId = AssetId.of("ID1000109507");
        LocalDate openDate = LocalDate.now();
        Money openPrice = Money.of(new BigDecimal("10000"));
        Money openFee = Money.of(new BigDecimal("1500"));
        Quantity initialQuantity = Quantity.ofShares(new BigDecimal("1000"));

        return Stream.of(
                Arguments.of(null, portfolioId, assetId, openDate, openPrice, openFee, initialQuantity, "id cannot be null"),
                Arguments.of(id, null, assetId, openDate, openPrice, openFee, initialQuantity, "portfolioId cannot be null"),
                Arguments.of(id, portfolioId, null, openDate, openPrice, openFee, initialQuantity, "assetId cannot be null"),
                Arguments.of(id, portfolioId, assetId, null, openPrice, openFee, initialQuantity, "openDate cannot be null"),
                Arguments.of(id, portfolioId, assetId, openDate, null, openFee, initialQuantity, "openPrice cannot be null"),
                Arguments.of(id, portfolioId, assetId, openDate, openPrice, null, initialQuantity, "openFee cannot be null"),
                Arguments.of(id, portfolioId, assetId, openDate, openPrice, openFee, null, "initialQuantity cannot be null")
        );
    }

    @Test
    @DisplayName("Keep the given id when reconstitute")
    void should_keepGivenId_when_reconstitute() {
        // Given
        AcquisitionId id = AcquisitionId.generate();
        PortfolioId portfolioId = PortfolioId.generate();
        AssetId assetId = AssetId.of("ID1000109507");
        LocalDate openDate = LocalDate.now();
        Money openPrice = Money.of(new BigDecimal("10000"));
        Money openFee = Money.of(new BigDecimal("1500"));
        Quantity initialQuantity = Quantity.ofShares(new BigDecimal("1000"));

        // When
        Acquisition result = Acquisition.reconstitute(id, portfolioId, assetId, openDate, openPrice, openFee, initialQuantity);

        // Then
        assertThat(result.id()).isEqualTo(id);
    }
}
