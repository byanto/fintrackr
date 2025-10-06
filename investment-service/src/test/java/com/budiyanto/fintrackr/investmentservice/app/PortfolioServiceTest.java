package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.PortfolioResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.UpdatePortfolioRequest;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.PortfolioMapper;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.repository.PortfolioRepository;


@ExtendWith(MockitoExtension.class)
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioMapper portfolioMapper;

    @InjectMocks
    private PortfolioService portfolioService;

    @Test
    void shouldCreatePortfolio() {
        // Arrange
        String portfolioName = "Test Portfolio";
        Long portfolioId = 1L;

        Portfolio savedPortfolio = new Portfolio(portfolioName);
        ReflectionTestUtils.setField(savedPortfolio, "id", portfolioId);        
        when(portfolioRepository.save(eq(new Portfolio(portfolioName)))).thenReturn(savedPortfolio);
        
        PortfolioResponse responseDto = new PortfolioResponse(portfolioId, portfolioName, Instant.now());
        when(portfolioMapper.toDto(eq(savedPortfolio))).thenReturn(responseDto);     

        // Act
        PortfolioResponse result = portfolioService.createPortfolio(new CreatePortfolioRequest(portfolioName));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(portfolioId);
        assertThat(result.name()).isEqualTo(portfolioName);

    }  

    @Test
    void shouldReturnPortfolioWhenFound() {
        // Arrange
        Long portfolioId = 1L;
        String portfolioName = "Test Portfolio";

        Portfolio retrievedPortfolio = new Portfolio(portfolioName);
        ReflectionTestUtils.setField(retrievedPortfolio, "id", portfolioId);

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(retrievedPortfolio));
        
        PortfolioResponse responseDto = new PortfolioResponse(portfolioId, portfolioName, Instant.now());
        when(portfolioMapper.toDto(eq(retrievedPortfolio))).thenReturn(responseDto);  

        // Act
        PortfolioResponse result = portfolioService.retrievePortfolioById(portfolioId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(portfolioId);
        assertThat(result.name()).isEqualTo(portfolioName);
    }

    @Test
    void shouldThrowExceptionWhenPortfolioNotFound() {
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

    @Test
    void shouldReturnAllPortfolios() {
        // Arrange
        String portfolioName1 = "Test Portfolio 1";
        String portfolioName2 = "Test Portfolio 2";

        Portfolio portfolio1 = new Portfolio(portfolioName1);
        Portfolio portfolio2 = new Portfolio(portfolioName2);
        List<Portfolio> portfolioList = List.of(portfolio1, portfolio2);
        when(portfolioRepository.findAll()).thenReturn(portfolioList);

        PortfolioResponse responseDto1 = new PortfolioResponse(1L, portfolioName1, Instant.now());
        PortfolioResponse responseDto2 = new PortfolioResponse(2L, portfolioName2, Instant.now());
        List<PortfolioResponse> responseDtoList = List.of(responseDto1, responseDto2);
        when(portfolioMapper.toDtoList(eq(portfolioList))).thenReturn(responseDtoList);  
        
        // Act
        List<PortfolioResponse> result = portfolioService.retrieveAllPortfolios();
        
        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo(portfolioName1);
        assertThat(result.get(1).name()).isEqualTo(portfolioName2);   
    }

    @Test
    void shouldUpdatePortfolioWhenExists() {
        // Arrange
        Long portfolioId = 1L;
        String updatedPortfolioName = "Updated Portfolio";

        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(new Portfolio("Existing Portfolio")));
        when(portfolioRepository.save(any(Portfolio.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        PortfolioResponse responseDto = new PortfolioResponse(portfolioId, updatedPortfolioName, Instant.now());
        when(portfolioMapper.toDto(any(Portfolio.class))).thenReturn(responseDto);

        // Act
        PortfolioResponse result = portfolioService.updatePortfolio(portfolioId, new UpdatePortfolioRequest(updatedPortfolioName));

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(portfolioId);
        assertThat(result.name()).isEqualTo(updatedPortfolioName);
    }

    void shouldThrowExceptionWhenUpdatingNonExistentPortfolio() {
        // Arrange
        Long portfolioId = 1L;
        UpdatePortfolioRequest request = new UpdatePortfolioRequest("Updated Portfolio");

        // Act
        when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());

        // Assert
        assertThatThrownBy(() -> portfolioService.updatePortfolio(portfolioId, request))
            .isInstanceOf(PortfolioNotFoundException.class)
            .asInstanceOf(InstanceOfAssertFactories.type(PortfolioNotFoundException.class))
            .extracting(PortfolioNotFoundException::getPortfolioId)
            .isEqualTo(portfolioId);
        verify(portfolioRepository, never()).save(any(Portfolio.class));

    }

    @Test
    void shouldDeletePortfolio() {
        // Arrange
        Long portfolioId = 1L;

        // Act
        portfolioService.deletePortfolioById(portfolioId);

        // Assert
        verify(portfolioRepository, times(1)).deleteById(portfolioId);
    }

}
