package com.budiyanto.fintrackr.investmentservice.app;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.investmentservice.app.exception.InsufficientHoldingsException;
import com.budiyanto.fintrackr.investmentservice.domain.Holding;
import com.budiyanto.fintrackr.investmentservice.domain.Trade;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.repository.HoldingRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HoldingService {

    private final HoldingRepository holdingRepository;

    @Transactional(propagation = Propagation.MANDATORY)
    public void processTrade(Trade trade) {
        Holding holding = holdingRepository
                .findByPortfolioIdAndInstrumentId(trade.getPortfolio().getId(), trade.getInstrument().getId())
                .orElse(new Holding(trade.getPortfolio(), trade.getInstrument(), BigDecimal.ZERO, BigDecimal.ZERO));
        
        if (trade.getTradeType() == TradeType.BUY) {
            holding.add(trade.getQuantity(), trade.getPrice());
        } else if (trade.getTradeType() == TradeType.SELL) {
            if (holding.getQuantity().compareTo(trade.getQuantity()) < 0) {
                throw new InsufficientHoldingsException(
                    trade.getQuantity(), holding.getQuantity(),
                    trade.getPortfolio().getId(), trade.getInstrument().getId()
                );
            }
            holding.subtract(trade.getQuantity());
        }

        // If the holding quantity is now zero after a sell, it can be removed.
        // Otherwise, save the new or updated holding.
        if (holding.getQuantity().compareTo(BigDecimal.ZERO) == 0 && holding.getId() != null) {
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }
    }

}
