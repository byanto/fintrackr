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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
@DisplayName("PortfolioService Tests")
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private PortfolioMapper portfolioMapper;

    @InjectMocks
    private PortfolioService portfolioService;

    @Nested
    @DisplayName("createPortfolio method")
    class CreatePortfolio {
        
        @Test
        @DisplayName("should create and return a new portfolio")
        void should_createAndReturnNewPortfolio() {
            // Arrange
            Long portfolioId = 1L;
            String portfolioName = "Test Portfolio";
            Instant createdAt = Instant.now();

            // Mapper converts request DTO to a transient entity (no ID yet)
            Portfolio transientPortfolio = new Portfolio(portfolioName);
            when(portfolioMapper.toPortfolio(any(CreatePortfolioRequest.class)))
                .thenReturn(transientPortfolio);

            // Repository saves the transient entity and returns a persisted one (with an ID)
            Portfolio savedPortfolio = new Portfolio(portfolioName);
            ReflectionTestUtils.setField(savedPortfolio, "id", portfolioId);
            ReflectionTestUtils.setField(savedPortfolio, "createdAt", createdAt);
            when(portfolioRepository.save(any(Portfolio.class))).thenReturn(savedPortfolio);

            // Mapper converts the persisted entity to a response DTO
            PortfolioResponse responseDto = new PortfolioResponse(portfolioId, portfolioName, createdAt);
            when(portfolioMapper.toResponseDto(savedPortfolio)).thenReturn(responseDto);
            
            // Act
            CreatePortfolioRequest request = new CreatePortfolioRequest(portfolioName);
            PortfolioResponse result = portfolioService.createPortfolio(request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(portfolioId);
            assertThat(result.name()).isEqualTo(portfolioName);
            assertThat(result.createdAt()).isEqualTo(createdAt);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
            verify(portfolioRepository, times(1)).save(captor.capture());
            Portfolio capturedPortfolio = captor.getValue();

            assertThat(capturedPortfolio).isSameAs(transientPortfolio); // It's the exact same instance
            assertThat(capturedPortfolio.getId()).isNull(); // Should be the transient entity
            assertThat(capturedPortfolio.getName()).isEqualTo(portfolioName);
            assertThat(capturedPortfolio.getCreatedAt()).isNull(); // Should be the transient entity
        }
    }

    @Nested
    @DisplayName("retrievePortfolioById method")
    class RetrievePortfolioById {
        @Test
        @DisplayName("should return portfolio when ID exists")
        void should_returnPortfolio_when_idExists() {
            // Arrange
            Long portfolioId = 1L;
            String portfolioName = "Test Portfolio";

            Portfolio retrievedPortfolio = new Portfolio(portfolioName);
            ReflectionTestUtils.setField(retrievedPortfolio, "id", portfolioId);

            when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(retrievedPortfolio));
            
            PortfolioResponse responseDto = new PortfolioResponse(portfolioId, portfolioName, Instant.now());
            when(portfolioMapper.toResponseDto(any(Portfolio.class))).thenReturn(responseDto);  

            // Act
            PortfolioResponse result = portfolioService.retrievePortfolioById(portfolioId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(portfolioId);
            assertThat(result.name()).isEqualTo(portfolioName);
        }

        @Test
        @DisplayName("should throw PortfolioNotFoundException when ID does not exist")
        void should_throwException_when_retrievingNonExistentPortfolio() {
            // Arrange
            Long portfolioId = 1L;
            
            when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.empty());        

            // Act & Assert
            assertThatThrownBy(() -> portfolioService.retrievePortfolioById(portfolioId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(PortfolioNotFoundException.class))
                .extracting(PortfolioNotFoundException::getPortfolioId)
                .isEqualTo(portfolioId);
        }
    }

    @Nested
    @DisplayName("retrieveAllPortfolios method")
    class RetrieveAllPortfolios {
        @Test
        @DisplayName("should return a list of all portfolios")
        void should_returnAllPortfolios() {
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
            when(portfolioMapper.toReponseDtoList(eq(portfolioList))).thenReturn(responseDtoList);  
            
            // Act
            List<PortfolioResponse> result = portfolioService.retrieveAllPortfolios();
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result.get(0).name()).isEqualTo(portfolioName1);
            assertThat(result.get(1).name()).isEqualTo(portfolioName2);   
        }
    }

    @Nested
    @DisplayName("updatePortfolio method")
    class UpdatePortfolio {
        @Test
        @DisplayName("should update portfolio when ID exists")
        void should_updatePortfolio_when_idExists() {
            // Arrange
            Long portfolioId = 1L;
            String existingPortfolioName = "Existing Portfolio";
            String updatedPortfolioName = "Updated Portfolio";
            Instant createdAt = Instant.now();
            
            // Repository find the Entity by id
            Portfolio existingPortfolio = new Portfolio(existingPortfolioName);
            ReflectionTestUtils.setField(existingPortfolio, "id", portfolioId);
            ReflectionTestUtils.setField(existingPortfolio, "createdAt", createdAt);
            when(portfolioRepository.findById(portfolioId)).thenReturn(Optional.of(existingPortfolio));

            // When save is called, we can just return the same instance that was passed to it
            when(portfolioRepository.save(any(Portfolio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Mapper will convert the updated entity to a response DTO
            PortfolioResponse response = new PortfolioResponse(portfolioId, updatedPortfolioName, createdAt);
            when(portfolioMapper.toResponseDto(any(Portfolio.class))).thenReturn(response);
            
            // Act
            UpdatePortfolioRequest request = new UpdatePortfolioRequest(updatedPortfolioName);
            PortfolioResponse result = portfolioService.updatePortfolio(portfolioId, request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(portfolioId);
            assertThat(result.name()).isEqualTo(updatedPortfolioName);
            assertThat(result.createdAt()).isEqualTo(createdAt);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
            verify(portfolioRepository, times(1)).save(captor.capture());
            Portfolio capturedPortfolio = captor.getValue();
            
            assertThat(capturedPortfolio).isSameAs(existingPortfolio); // It's the exact same instance
            assertThat(capturedPortfolio.getId()).isEqualTo(portfolioId);
            assertThat(capturedPortfolio.getName()).isEqualTo(updatedPortfolioName);
            assertThat(capturedPortfolio.getCreatedAt()).isEqualTo(createdAt); // The original createdAt should be preserved
        }

        @Test
        @DisplayName("should throw PortfolioNotFoundException when ID does not exist")
        void should_throwException_when_updatingNonExistentPortfolio() {
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
    }

    @Nested
    @DisplayName("deletePortfolioById method")
    class DeletePortfolio {
        @Test
        @DisplayName("should delete portfolio")
        void shouldDeletePortfolio() {
            // Arrange
            Long portfolioId = 1L;

            // Act
            portfolioService.deletePortfolioById(portfolioId);

            // Assert
            verify(portfolioRepository, times(1)).deleteById(portfolioId);
        }
    }

}
