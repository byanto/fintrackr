package com.budiyanto.fintrackr.investmentservice.app;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.repository.FeeRuleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FeeService {

    private final FeeRuleRepository feeRuleRepository;

    public BigDecimal calculateFee(BrokerAccount brokerAccount, Instrument instrument, TradeType tradeType, BigDecimal quantity, BigDecimal price) {
        BigDecimal transactionValue = quantity.multiply(price);

        // Find the fee rule for this broker, instrument type, AND trade type
        return feeRuleRepository.findByBrokerAccountIdAndInstrumentTypeAndTradeType(
                brokerAccount.getId(),
                instrument.getInstrumentType(),
                tradeType
        ).map(feeRule -> {
            // Calculate the fee based on the percentage
            BigDecimal calculatedFee = transactionValue.multiply(feeRule.getFeePercentage());

            // Apply the minimum fee if it's defined and the calculated fee is lower
            if (feeRule.getMinFee() != null && calculatedFee.compareTo(feeRule.getMinFee()) < 0) {
                return feeRule.getMinFee();
            }
            return calculatedFee;
        }).orElse(BigDecimal.ZERO); // If no rule is found, default to zero fee
    }
}
