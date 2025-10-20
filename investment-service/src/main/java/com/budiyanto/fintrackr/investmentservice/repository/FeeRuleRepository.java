package com.budiyanto.fintrackr.investmentservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.budiyanto.fintrackr.investmentservice.domain.FeeRule;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;

public interface FeeRuleRepository extends JpaRepository<FeeRule, Long> {
    Optional<FeeRule> findByBrokerAccountIdAndInstrumentTypeAndTradeType(Long brokerAccountId, 
            InstrumentType instrumentType, TradeType tradeType);
}