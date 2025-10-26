package com.budiyanto.fintrackr.investmentservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.dto.PortfolioResponse;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {

    PortfolioResponse toResponseDto(Portfolio portfolio);
    List<PortfolioResponse> toResponseDtoList(List<Portfolio> portfolios);
    
}
