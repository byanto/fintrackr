package com.budiyanto.fintrackr.investmentservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.budiyanto.fintrackr.investmentservice.domain.Trade;
import com.budiyanto.fintrackr.investmentservice.dto.TradeResponse;

@Mapper(componentModel = "spring")
public interface TradeMapper {
    
    @Mapping(target = "portfolioId", source = "portfolio.id")
    @Mapping(target = "instrumentId", source = "instrument.id")
    TradeResponse toResponseDto(Trade trade);

    List<TradeResponse> toResponseDtoList(List<Trade> trades);


}
