package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.repository.PortfolioRepository;


@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void shouldCreatePortfolio() {
        // Arrange
        String portfolioName = "Test Portfolio";
        Long portfolioId = 1L;
        
        when(portfolioRepository.save(eq(new Portfolio(portfolioName)))).thenAnswer(invocation -> {
            // Get the portfolio that was passed to the save method
            Portfolio savedPortfolio = invocation.getArgument(0);
            
            // Simulate the database action: use reflection to assing portfolio id
            ReflectionTestUtils.setField(savedPortfolio, "id", portfolioId);

            // Return the object, but now with an ID, simulating the database behaviour
            return savedPortfolio;
        });

        // Act
        Portfolio createdPortfolio = portfolioService.createPortfolio(new CreatePortfolioRequest(portfolioName));

        // Assert
        assertThat(createdPortfolio).isNotNull();
        assertThat(createdPortfolio.getId()).isEqualTo(portfolioId);
        assertThat(createdPortfolio.getName()).isEqualTo(portfolioName);

    }  

    @Test
    void shouldReturnPortfolioWhenFound() {
        // Arrange
        Long portfolioId = 1L;
        String portfolioName = "Test Portfolio";

        when(portfolioRepository.findById(eq(portfolioId))).thenAnswer(invocation -> {
            Portfolio portfolio = new Portfolio(portfolioName);
            ReflectionTestUtils.setField(portfolio, "id", portfolioId);
            return Optional.of(portfolio);
        });

        // Act
        Portfolio retrievedPortfolio = portfolioService.retrievePortfolioById(portfolioId);

        // Assert
        assertThat(retrievedPortfolio).isNotNull();
        assertThat(retrievedPortfolio.getId()).isEqualTo(portfolioId);
        assertThat(retrievedPortfolio.getName()).isEqualTo(portfolioName);
    }

    @Test
    void shouldThrowExceptionWhenNotFound() {
        // Arrange
        Long portfolioId = 1L;
        
        when(portfolioRepository.findById(eq(portfolioId))).thenReturn(Optional.empty());        

        // Act & Assert
        assertThatThrownBy(() -> portfolioService.retrievePortfolioById(portfolioId))
            .isInstanceOf(PortfolioNotFoundException.class)
            .asInstanceOf(InstanceOfAssertFactories.type(PortfolioNotFoundException.class))
            .extracting(PortfolioNotFoundException::getPortfolioId)
            .isEqualTo(portfolioId);
    }


}
