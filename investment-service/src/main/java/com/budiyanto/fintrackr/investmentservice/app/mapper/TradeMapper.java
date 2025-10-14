package com.budiyanto.fintrackr.investmentservice.app.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budiyanto.fintrackr.investmentservice.api.dto.TradeResponse;
import com.budiyanto.fintrackr.investmentservice.domain.Trade;

@Mapper(componentModel = "spring")
public interface TradeMapper {
    
    @Mapping(target = "portfolioId", source = "portfolio.id")
    @Mapping(target = "instrumentId", source = "instrument.id")
    TradeResponse toResponseDto(Trade trade);

}
