package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.domain.FeeRule;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;

@DisplayName("FeeRuleRepository Tests")
class FeeRuleRepositoryTest extends AbstractRepositoryTest {

    private final FeeRuleRepository feeRuleRepository;
    private final BrokerAccountRepository brokerAccountRepository;

    @Autowired
    FeeRuleRepositoryTest(FeeRuleRepository feeRuleRepository, BrokerAccountRepository brokerAccountRepository) {
        this.feeRuleRepository = feeRuleRepository;
        this.brokerAccountRepository = brokerAccountRepository;
    }

    @Test
    @DisplayName("should save and retrieve fee rule")
    void should_saveAndRetrieveFeeRule() {
        // Arrange: Create a new FeeRule object
        BrokerAccount unsavedBrokerAccount = new BrokerAccount("My Broker Account", "My Broker");
        BrokerAccount savedBrokerAccount = brokerAccountRepository.save(unsavedBrokerAccount);
        InstrumentType instrumentType = InstrumentType.STOCK;
        TradeType tradeType = TradeType.BUY;
        BigDecimal feePercentage = new BigDecimal("0.0018"); // 0.18%
        BigDecimal minFee = new BigDecimal("10000"); // IDR 100.000

        FeeRule feeRule = new FeeRule(savedBrokerAccount, instrumentType, tradeType, feePercentage, minFee);

        // Act: Save the fee rule using the repository
        FeeRule savedFeeRule = feeRuleRepository.save(feeRule);

        // Assert: Verify that the portfolio was saved correctly
        assertThat(savedFeeRule).isNotNull();
        assertThat(savedFeeRule.getId()).isGreaterThan(0);

        // Act: Retrieve the saved fee rule
        FeeRule retrievedFeeRule = feeRuleRepository.findById(savedFeeRule.getId()).orElse(null);

        // Assert: Verify that the fee rule can be retrieved
        assertThat(retrievedFeeRule).isNotNull();
        assertThat(retrievedFeeRule.getId()).isEqualTo(savedFeeRule.getId());
        assertThat(retrievedFeeRule.getBrokerAccount().getId()).isEqualTo(savedBrokerAccount.getId());
        assertThat(retrievedFeeRule.getInstrumentType()).isEqualTo(instrumentType);
        assertThat(retrievedFeeRule.getTradeType()).isEqualTo(tradeType);
        assertThat(retrievedFeeRule.getFeePercentage()).isEqualByComparingTo(feePercentage);
        assertThat(retrievedFeeRule.getMinFee()).isEqualByComparingTo(minFee);
        assertThat(retrievedFeeRule.getCreatedAt()).isNotNull();
        
    }

}
