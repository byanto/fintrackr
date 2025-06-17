package com.byanto.fintrackr.investment.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.byanto.fintrackr.investment.portfolio.dto.PortfolioRequest;
import com.byanto.fintrackr.investment.portfolio.model.Portfolio;

@RestController
@RequestMapping("/api/investment/portfolios")
public class PortfolioController {

	private final PortfolioService portfolioService;
	
	public PortfolioController(PortfolioService portfolioService) {
		this.portfolioService = portfolioService;
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Portfolio createPortfolio(@RequestBody PortfolioRequest request) {
		return portfolioService.createPortfolio(request);
	}
}
