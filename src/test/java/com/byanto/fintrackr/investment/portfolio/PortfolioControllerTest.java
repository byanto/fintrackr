package com.byanto.fintrackr.investment.portfolio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.service.annotation.PostExchange;

import com.byanto.fintrackr.investment.portfolio.dto.PortfolioRequest;
import com.byanto.fintrackr.investment.portfolio.model.Portfolio;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PortfolioController.class)
@DisplayName("Portfolio API Tests")
class PortfolioControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private PortfolioService portfolioService;
	
	// TEST: Create a new portfolio
	@Nested 
	@DisplayName("Create Portfolio Endpoint Tests")
	class CreatePortfolio {
		
		@Test
		@DisplayName("GIVEN a valid portfolio request WHEN the POST endpoint is called THEN a new portfolio is created with status 201")
		void shouldReturn201AndPortfolio_whenRequestIsValid() throws Exception{
			// Arrange
			var request = new PortfolioRequest("My Retirement", "Primary retirement savings portfolio.");
			var createdPortfolio = new Portfolio("My Retirement", "Primary retirement savings portfolio.");
			createdPortfolio.setId(1L); // Simulate that the portfolio was saved and got an ID
			
			// Act
			when(portfolioService.createPortfolio(any(PortfolioRequest.class))).thenReturn(createdPortfolio);
			
			// Assert
			mockMvc.perform(post("/api/investment/portfolios")
					.contentType(MediaType.APPLICATION_JSON)
					.content(objectMapper.writeValueAsString(request)))
					.andExpect(status().isCreated())
					.andExpect(jsonPath("$.id").value(createdPortfolio.getId()))
					.andExpect(jsonPath("$.name").value(createdPortfolio.getName()))
					.andExpect(jsonPath("$.description").value(createdPortfolio.getDescription()));
		}
	}
	
	// TEST: Get a single portfolio
	
	// TEST: Get all portfolios
	
	// TEST: Update a portfolio
	
	// TEST: Delete a portfolio
	
	// TEST: Handle invalid requests
	
	
	
}
