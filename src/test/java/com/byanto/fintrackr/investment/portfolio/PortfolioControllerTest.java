package com.byanto.fintrackr.investment.portfolio;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.byanto.fintrackr.investment.portfolio.dto.PortfolioRequest;
import com.byanto.fintrackr.investment.portfolio.model.Portfolio;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(PortfolioController.class)
@DisplayName("Portfolio API Tests")
class PortfolioControllerTest {

	@Autowired
	private MockMvc mockMvc;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@MockitoBean
	private PortfolioService portfolioService;
	
	// TEST: Create a new portfolio (POST /api/investment/portfolios)
	@Nested 
	@DisplayName("Create Portfolio Endpoint Tests")
	class CreatePortfolio {
		
		@Test
		@DisplayName("GIVEN a valid portfolio request WHEN the POST endpoint is called THEN a new portfolio is created with status 201")
		void shouldReturnStatus201AndPortfolio_whenRequestIsValid() throws Exception{
			// Arrange
			var request = new PortfolioRequest("My Retirement", "Primary retirement savings portfolio.");
			var createdPortfolio = new Portfolio(request.name(), request.description());
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
	
	// TEST: Get a single portfolio (GET /api/investment/portfolios/{id})
	@Nested
	@DisplayName("Get Portfolio By Id Endpoint Tests")
	class GetPortfolioById {
		
		@Test
		@DisplayName("GIVEN a valid portfolio id WHEN the GET endpoint is called THEN the portfolio is returned with status 200 OK")
		void shouldReturnStatus200AndPortfolio_whenPortfolioExists() throws Exception{
			// Arrange
			var portfolioId = 1L;
			var expectedPortfolio = new Portfolio("My Retirement", "Primary retirement savings portfolio.");
			expectedPortfolio.setId(portfolioId);
			
			// Act
			when(portfolioService.retrievePortfolioById(portfolioId)).thenReturn(Optional.of(expectedPortfolio));
			
			// Assert
			mockMvc.perform(get("/api/investment/portfolios/{id}", portfolioId))
					.andExpect(status().isOk())
					.andExpect(jsonPath("$.id").value(expectedPortfolio.getId()))
					.andExpect(jsonPath("$.name").value(expectedPortfolio.getName()))
					.andExpect(jsonPath("$.description").value(expectedPortfolio.getDescription()));
		}
		
		@Test
		@DisplayName("GIVEN an invalid portfolio id WHEN the GET endpoint is called THEN status 404 is returned")
		void shouldReturnStatus404_whenPortfolioDoesNotExist() throws Exception {
			// Arrange
			var portfolioId = 1L;
			
			// Act
			when(portfolioService.retrievePortfolioById(portfolioId)).thenReturn(Optional.empty());
			
			// Assert
			mockMvc.perform(get("/api/investment/portfolios/{id}", portfolioId))
					.andExpect(status().isNotFound());
		}
	}
	
	// TEST: Get all portfolios (GET /api/investment/portfolios)
	
	// TEST: Update a portfolio
	
	// TEST: Delete a portfolio (DELETE /api/investment/portfolios/{id})
	@Nested
	@DisplayName("Delete Portfolio By Id Endpoint Tests")
	class DeletePortfolioById {
		
		@Test
		@DisplayName("GIVEN a valid portfolio id WHEN the DELETE endpoint is called THEN the portfolio is deleted with status 204 No Content")
		void shouldReturnStatus204NoContent_whenPortfolioExists() throws Exception{
			// Arrange
			var portfolioId = 1L;
			
			// Act
			when(portfolioService.deletePortfolioById(portfolioId)).thenReturn(true);
			
			// Assert
			mockMvc.perform(delete("/api/investment/portfolios/{id}", portfolioId))
					.andExpect(status().isNoContent());
			verify(portfolioService, times(1)).deletePortfolioById(portfolioId);
		}
		
		@Test
		@DisplayName("GIVEN an invalid portfolio id WHEN the DELETE endpoint is called THEN status 404 is returned")
		void shouldReturnStatus404_whenPortfolioDoesNotExist() throws Exception {
			// Arrange
			var portfolioId = 1L;
			
			// Act
			when(portfolioService.deletePortfolioById(portfolioId)).thenReturn(false);
			
			// Assert
			mockMvc.perform(delete("/api/investment/portfolios/{id}", portfolioId))
					.andExpect(status().isNotFound());
		}
	}
	
	// TEST: Handle invalid requests
	
	
	
}
