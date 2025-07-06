package com.byanto.fintrackr.investment.portfolio;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.byanto.fintrackr.investment.portfolio.dto.PortfolioRequest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE) // Don't start a web server
@Testcontainers // This annotation is needed to manage the @Container lifecycle
@ActiveProfiles("test")
class PortfolioServiceIntegrationTest {

	@Container
    @ServiceConnection // This is the magic annotation!
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
	
	private final PortfolioService portfolioService;
	private final PortfolioRepository portfolioRepository;
	
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
		assertEquals(request.name(), foundInDB.getName());
		assertEquals(request.description(), foundInDB.getDescription());
	}
	
	@Test
	@DisplayName("GIVEN an existing portfolio id WHEN the GET endpoint is called THEN the portfolio is returned")
	void shouldReturnPortfolio_whenRetrievingByExistingId() {
		// Arrange
		var request = new PortfolioRequest("Real DB Test", "Portfolio saved in a real DB.");
		var createdPortfolio = portfolioService.createPortfolio(request);
		
		// Act
		var foundPortfolio = portfolioService.retrievePortfolioById(createdPortfolio.getId());
		
		// Assert
		assertTrue(foundPortfolio.isPresent());
		assertEquals(createdPortfolio.getName(), foundPortfolio.get().getName());
		assertEquals(createdPortfolio.getDescription(), foundPortfolio.get().getDescription());
	}

}
