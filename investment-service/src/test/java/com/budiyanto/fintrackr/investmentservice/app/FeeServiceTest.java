package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.domain.FeeRule;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.repository.FeeRuleRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeeService Tests")
class FeeServiceTest {

    @Mock
    private FeeRuleRepository feeRuleRepository;

    @InjectMocks
    private FeeService feeService;

    private BrokerAccount brokerAccount;
    private Instrument stockInstrument;

    private static final Long BROKER_ACCOUNT_ID = 1L;
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.STOCK;
    private static final TradeType TRADE_TYPE = TradeType.BUY;
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.0015"); // 0.15%
    private static final BigDecimal QUANTITY = new BigDecimal("100");
    private static final BigDecimal PRICE = new BigDecimal("8000");

    @BeforeEach
    void setUp() {
        brokerAccount = new BrokerAccount("My Account", "Broker A");
        ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);

        stockInstrument = new Instrument(INSTRUMENT_TYPE, "BBCA", "Bank Central Asia", "IDR");
    }

    @Nested
    @DisplayName("calculateFee method")
    class CalculateFee {

        @Test
        @DisplayName("should calculate fee based on percentage when above minimum")
        void should_calculateFee_when_aboveMinimum() {
            // Arrange
            BigDecimal minFee = new BigDecimal("1000");
            FeeRule feeRule = new FeeRule(brokerAccount, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, minFee);

            when(feeRuleRepository.findByBrokerAccountIdAndInstrumentTypeAndTradeType(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE))
                        .thenReturn(Optional.of(feeRule));

            // Act
            BigDecimal calculatedFee = feeService.calculateFee(brokerAccount, stockInstrument, TRADE_TYPE, QUANTITY, PRICE);

            // Assert
            // Expected fee = (100 * 8000) * 0.0015 = 1200. This is > minFee of 1000.
            BigDecimal expectedFee = QUANTITY.multiply(PRICE).multiply(FEE_PERCENTAGE);
            assertThat(calculatedFee).isEqualByComparingTo(expectedFee);
        }

        @Test
        @DisplayName("should use minimum fee when calculated percentage is too low")
        void should_useMinimumFee_when_percentageIsTooLow() {
            // Arrange
            BigDecimal minFee = new BigDecimal("2000");
            FeeRule feeRule = new FeeRule(brokerAccount, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, minFee);

            when(feeRuleRepository.findByBrokerAccountIdAndInstrumentTypeAndTradeType(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE))
                    .thenReturn(Optional.of(feeRule));

            // Act
            BigDecimal calculatedFee = feeService.calculateFee(brokerAccount, stockInstrument, TRADE_TYPE, QUANTITY, PRICE);

            // Assert
            // Expected fee = (100 * 8000) * 0.0015 = 1200. This is < minFee of 2000.
            assertThat(calculatedFee).isEqualByComparingTo(minFee);
        }

        @Test
        @DisplayName("should return zero fee when no rule is found")
        void should_returnZero_when_noRuleIsFound() {
            // Arrange
            when(feeRuleRepository.findByBrokerAccountIdAndInstrumentTypeAndTradeType(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE))
                    .thenReturn(Optional.empty());

            // Act
            BigDecimal calculatedFee = feeService.calculateFee(brokerAccount, stockInstrument, TRADE_TYPE, QUANTITY, PRICE);

            // Assert
            assertThat(calculatedFee).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}