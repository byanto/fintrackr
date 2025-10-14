package com.budiyanto.fintrackr.investmentservice.app;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreateTradeRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.TradeResponse;
import com.budiyanto.fintrackr.investmentservice.app.exception.InstrumentNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.TradeNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.TradeMapper;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.domain.Trade;
import com.budiyanto.fintrackr.investmentservice.repository.InstrumentRepository;
import com.budiyanto.fintrackr.investmentservice.repository.PortfolioRepository;
import com.budiyanto.fintrackr.investmentservice.repository.TradeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TradeService {

    private final TradeRepository tradeRepository;
    private final PortfolioRepository portfolioRepository;
    private final InstrumentRepository instrumentRepository;
    private final TradeMapper tradeMapper;

    @Transactional
    public TradeResponse createTrade(CreateTradeRequest request) {
        // Fetch the related entities from repositories
        Portfolio portfolio = portfolioRepository.findById(request.portfolioId())
            .orElseThrow(() -> new PortfolioNotFoundException(request.portfolioId()));
        Instrument instrument = instrumentRepository.findById(request.instrumentId())
            .orElseThrow(() -> new InstrumentNotFoundException(request.instrumentId()));
        
        // Create the new Trade entity
        Trade trade = new Trade(
            portfolio,
            instrument,
            request.tradeType(),
            request.quantity(),
            request.price(),
            request.tradedAt()
        );

        // Save the new entity
        Trade savedTrade = tradeRepository.save(trade);

        // Map the entity to response
        return tradeMapper.toResponseDto(savedTrade);
    }

    @Transactional(readOnly = true)
    public TradeResponse retrieveTradeById(Long id) {
        Trade retrievedTrade = tradeRepository.findById(id)
            .orElseThrow(() -> new TradeNotFoundException(id));
        return tradeMapper.toResponseDto(retrievedTrade);
    }

    @Transactional(readOnly = true)
    public List<TradeResponse> retrieveAllTrades() { 
        List<Trade> allTrades = tradeRepository.findAll();
        return tradeMapper.toResponseDtoList(allTrades);
    
    }
}
