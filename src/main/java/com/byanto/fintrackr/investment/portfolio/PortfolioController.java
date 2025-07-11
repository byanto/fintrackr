package com.byanto.fintrackr.investment.portfolio;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.byanto.fintrackr.investment.portfolio.dto.PortfolioRequest;
import com.byanto.fintrackr.investment.portfolio.model.Portfolio;
import com.byanto.fintrackr.shared.exception.ResourceNotFoundException;

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
	
	@GetMapping("/{id}")
	public Portfolio retrievePortfolioById(@PathVariable Long id) {
		return portfolioService.retrievePortfolioById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + id));
	}
	
	@DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePortfolioById(@PathVariable Long id) {
		boolean isDeleted = portfolioService.deletePortfolioById(id);
        if (isDeleted) {
            return ResponseEntity.noContent().build(); // HTTP 204
        } else {
        	return ResponseEntity.notFound().build(); // HTTP 404
        }
    }
}
