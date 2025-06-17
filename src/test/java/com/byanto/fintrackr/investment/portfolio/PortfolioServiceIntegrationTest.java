package com.byanto.fintrackr.investment.portfolio;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.byanto.fintrackr.investment.portfolio.dto.PortfolioRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE) // Don't start a web server
@Testcontainers
class PortfolioServiceIntegrationTest {

	private PortfolioService portfolioService;
	
	private PortfolioRepository portfolioRepository;
	
	@Autowired
	PortfolioServiceIntegrationTest(PortfolioService portfolioService, PortfolioRepository portfolioRepository) {
		this.portfolioService = portfolioService;
		this.portfolioRepository = portfolioRepository;
	}
	
	@Test
	@DisplayName("GIVEN a valid portfolio request WHEN the POST endpoint is called THEN a new portfolio is persisted in the database")
	void shouldPersistPortfolioInDatabase_whenCreatingPortfolio() {
		// Arrange
		var request = new PortfolioRequest("Real DB Test", "Portfolio saved in a real DB.");
		
		// Act
		var savedPortfolio = portfolioService.createPortfolio(request);
		
		// Assert
		assertNotNull(savedPortfolio.getId());
		
		var foundInDB = portfolioRepository.findById(savedPortfolio.getId()).orElseThrow();
		assertEquals("Real DB Test", foundInDB.getName());
		assertEquals("Portfolio saved in a real DB.", foundInDB.getDescription());
	}

}
