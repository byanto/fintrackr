package com.budiyanto.fintrackr.investmentservice.app.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.PortfolioResponse;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;

@Mapper(componentModel = "spring")
public interface PortfolioMapper {
    Portfolio toPortfolio(CreatePortfolioRequest request);
    PortfolioResponse toDto(Portfolio portfolio);
    List<PortfolioResponse> toDtoList(List<Portfolio> portfolios);
    Portfolio toPortfolio(PortfolioResponse portfolioResponse);
}
