package com.budiyanto.fintrackr.investmentservice.api.dto;

import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;

import java.math.BigDecimal;
import java.time.Instant;

public record FeeRuleResponse(
        Long id,
        BrokerAccountInFeeRuleResponse brokerAccount,
        InstrumentType instrumentType,
        TradeType tradeType,
        BigDecimal feePercentage,
        BigDecimal minFee,
        Instant createdAt
) {
    public record BrokerAccountInFeeRuleResponse(
            Long id, String name, String brokerName
    ) {}
}
