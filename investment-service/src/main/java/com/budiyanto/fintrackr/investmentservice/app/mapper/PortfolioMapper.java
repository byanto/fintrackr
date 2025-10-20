package com.budiyanto.fintrackr.investmentservice.app.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.budiyanto.fintrackr.investmentservice.api.dto.PortfolioResponse;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {

    PortfolioResponse toResponseDto(Portfolio portfolio);
    List<PortfolioResponse> toReponseDtoList(List<Portfolio> portfolios);
    
}
