package com.byanto.fintrackr.investment.portfolio;

import org.springframework.stereotype.Service;

import com.byanto.fintrackr.investment.portfolio.dto.PortfolioRequest;
import com.byanto.fintrackr.investment.portfolio.model.Portfolio;

@Service
public class PortfolioService {
	
	private final PortfolioRepository portfolioRepository;
	
	public PortfolioService(PortfolioRepository portfolioRepository) {
		this.portfolioRepository = portfolioRepository;
	}

	public Portfolio createPortfolio(PortfolioRequest request) {
		Portfolio portfolio = new Portfolio(request.name(), request.description());
		return portfolioRepository.save(portfolio);
	}

}
