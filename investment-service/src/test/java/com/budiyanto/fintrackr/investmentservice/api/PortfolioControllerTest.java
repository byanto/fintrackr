package com.budiyanto.fintrackr.investmentservice.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.PortfolioResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.UpdatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.app.PortfolioService;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;


@WebMvcTest(PortfolioController.class)
class PortfolioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PortfolioService portfolioService;

    /**
     * Tests that a portfolio can be created successfully via a POST request to /api/portfolios.
     * It verifies that the HTTP status is 201 (Created) and the response body
     * contains the correct portfolio ID and name.
     */
    @Test
    void should_createPortfolio() throws Exception {
        // Arrange
        Long portfolioId = 1L;
        String portfolioName = "Test Create Portfolio";
        
        CreatePortfolioRequest request = new CreatePortfolioRequest(portfolioName);
        PortfolioResponse response = new PortfolioResponse(portfolioId, portfolioName, Instant.now());
    
        when(portfolioService.createPortfolio(any(CreatePortfolioRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/portfolios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(portfolioId))
                .andExpect(jsonPath("$.name").value(portfolioName)); 

    }

    @Test
    void should_returnPortfolio_when_idExists() throws Exception{
        // Arrange
        Long portfolioId = 1L;
        String portfolioName = "Test Portfolio";
        PortfolioResponse response = new PortfolioResponse(portfolioId, portfolioName, Instant.now());

        when(portfolioService.retrievePortfolioById(portfolioId)).thenReturn(response);
        
        // Act & Assert
        mockMvc.perform(get("/api/portfolios/{id}", portfolioId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(portfolioId))
                .andExpect(jsonPath("$.name").value(portfolioName));
    }

    @Test
    void should_returnNotFound_when_retrievingNonExistentPortfolio() throws Exception {
        // Arrange
        Long portfolioId = 1L;
        when(portfolioService.retrievePortfolioById(portfolioId)).thenThrow(new PortfolioNotFoundException(portfolioId));
        
        // Act & Assert
        mockMvc.perform(get("/api/portfolios/{id}", portfolioId))
                .andExpect(status().isNotFound());
    }

    @Test
    void should_returnAllPortfolios() throws Exception {
        // Arrange
        Long portfolioId1 = 1L;
        Long portfolioId2 = 2L;
        String portfolioName1 = "Test Portfolio 1";
        String portfolioName2 = "Test Portfolio 2";
        PortfolioResponse response1 = new PortfolioResponse(portfolioId1, portfolioName1, Instant.now());
        PortfolioResponse response2 = new PortfolioResponse(portfolioId2, portfolioName2, Instant.now());
        when(portfolioService.retrieveAllPortfolios()).thenReturn(List.of(response1, response2));

        // Act & Assert
        mockMvc.perform(get("/api/portfolios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(portfolioId1))
                .andExpect(jsonPath("$[0].name").value(portfolioName1))
                .andExpect(jsonPath("$[1].id").value(portfolioId2))
                .andExpect(jsonPath("$[1].name").value(portfolioName2));
    }

    @Test
    void should_updatePortfolio_whenIdExists() throws Exception {
        // Arrange
        Long portfolioId = 1L;
        String updatedPortfolioName = "Updated Portfolio Name";

        UpdatePortfolioRequest request = new UpdatePortfolioRequest(updatedPortfolioName);
        PortfolioResponse response = new PortfolioResponse(portfolioId, updatedPortfolioName, Instant.now());
        when(portfolioService.updatePortfolio(portfolioId, any(UpdatePortfolioRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/portfolios/{id}", portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(portfolioId))
                .andExpect(jsonPath("$.name").value(updatedPortfolioName));
    }

    @Test
    void should_returnNotFound_when_updatingNonExistentPortfolio() throws Exception {
        // Arrange
        Long portfolioId = 1L;
        UpdatePortfolioRequest request = new UpdatePortfolioRequest("Updated Portfolio Name");
    
        // Act & Assert
        mockMvc.perform(put("/api/portfolios/{id}", portfolioId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());

        verify(portfolioService).updatePortfolio(portfolioId, request);
    }

    @Test
    void should_deletePortfolio_when_idExists() throws Exception {
        // Arrange
        Long portfolioId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/portfolios/{id}", portfolioId))
               .andExpect(status().isNoContent());
    
        verify(portfolioService).deletePortfolioById(portfolioId);
    }
}
