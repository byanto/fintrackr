package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import com.budiyanto.fintrackr.investmentservice.app.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.PortfolioNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.PortfolioMapper;
import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.domain.Portfolio;
import com.budiyanto.fintrackr.investmentservice.repository.BrokerAccountRepository;
import com.budiyanto.fintrackr.investmentservice.repository.PortfolioRepository;


@ExtendWith(MockitoExtension.class)
@DisplayName("PortfolioService Tests")
class PortfolioServiceTest {

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private BrokerAccountRepository brokerAccountRepository;

    @Mock
    private PortfolioMapper portfolioMapper;

    @InjectMocks
    private PortfolioService portfolioService;

    private static final Long PORTFOLIO_ID = 1L;
    private static final String PORTFOLIO_NAME = "Test Portfolio";
    private static final Long BROKER_ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NAME = "Test Broker Account";
    private static final String BROKER_NAME = "Broker A";


    @Nested
    @DisplayName("createPortfolio method")
    class CreatePortfolio {
        
        @Test
        @DisplayName("should create and return a new portfolio")
        void should_createAndReturnNewPortfolio() {
            // Arrange
            Instant createdAt = Instant.now();
            CreatePortfolioRequest request = new CreatePortfolioRequest(PORTFOLIO_NAME, BROKER_ACCOUNT_ID);
            
            BrokerAccount brokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            when(brokerAccountRepository.findById(BROKER_ACCOUNT_ID)).thenReturn(Optional.of(brokerAccount));

            // Repository saves the transient entity and returns a persisted one (with an ID)
            Portfolio savedPortfolio = new Portfolio(PORTFOLIO_NAME, brokerAccount);
            ReflectionTestUtils.setField(savedPortfolio, "id", PORTFOLIO_ID);
            ReflectionTestUtils.setField(savedPortfolio, "createdAt", createdAt);
            when(portfolioRepository.save(any(Portfolio.class))).thenReturn(savedPortfolio); // any() is fine here as we assert on the captor

            // Mapper converts the persisted entity to a response DTO
            var brokerDto = new PortfolioResponse.BrokerAccountInPortfolioResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            PortfolioResponse response = new PortfolioResponse(PORTFOLIO_ID, PORTFOLIO_NAME, brokerDto, createdAt);
            when(portfolioMapper.toResponseDto(savedPortfolio)).thenReturn(response);
            
            // Act
            PortfolioResponse result = portfolioService.createPortfolio(request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
            verify(portfolioRepository).save(captor.capture());
            Portfolio capturedPortfolio = captor.getValue();

            assertThat(capturedPortfolio.getId()).isNull(); // Should be the transient entity
            assertThat(capturedPortfolio.getName()).isEqualTo(PORTFOLIO_NAME);
            assertThat(capturedPortfolio.getBrokerAccount()).isSameAs(brokerAccount);
            assertThat(capturedPortfolio.getCreatedAt()).isNull(); // Should be the transient entity

            // Verify interactions
            verify(portfolioMapper).toResponseDto(savedPortfolio);
        }

        @Test
        @DisplayName("should throw exception when broker account not found")
        void should_throwException_when_brokerAccountNotFound() {
            // Arrange
            Long nonExistentId = 99L;
            CreatePortfolioRequest request = new CreatePortfolioRequest(PORTFOLIO_NAME, nonExistentId);
            when(brokerAccountRepository.findById(nonExistentId)).thenReturn(Optional.empty());        

            // Act & Assert
            assertThatThrownBy(() -> portfolioService.createPortfolio(request))
                .isInstanceOf(BrokerAccountNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(BrokerAccountNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });

            // Verify further interactions never occured
            verify(portfolioRepository, never()).save(any(Portfolio.class));
            verify(portfolioMapper, never()).toResponseDto(any(Portfolio.class));
        }
    }

    @Nested
    @DisplayName("retrievePortfolioById method")
    class RetrievePortfolioById {

        @Test
        @DisplayName("should return portfolio when ID exists")
        void should_returnPortfolio_when_idExists() {
            // Arrange            
            Instant createdAt = Instant.now();
            BrokerAccount brokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(brokerAccount, "createdAt", createdAt);

            // Repository finds the portfolio based on the given ID
            Portfolio retrievedPortfolio = new Portfolio(PORTFOLIO_NAME, brokerAccount);
            ReflectionTestUtils.setField(retrievedPortfolio, "id", PORTFOLIO_ID);
            ReflectionTestUtils.setField(retrievedPortfolio, "createdAt", createdAt);
            when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(retrievedPortfolio));
            
            // Mapper converts the retrieved entity to a response DTO
            var brokerDto = new PortfolioResponse.BrokerAccountInPortfolioResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            PortfolioResponse response = new PortfolioResponse(PORTFOLIO_ID, PORTFOLIO_NAME, brokerDto, createdAt);
            when(portfolioMapper.toResponseDto(retrievedPortfolio)).thenReturn(response);  

            // Act
            PortfolioResponse result = portfolioService.retrievePortfolioById(PORTFOLIO_ID);

            // Assert on the returned DTO
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Verify interactions
            verify(portfolioRepository).findById(PORTFOLIO_ID);
            verify(portfolioMapper).toResponseDto(retrievedPortfolio);
        }

        @Test
        @DisplayName("should throw PortfolioNotFoundException when ID does not exist")
        void should_throwException_when_retrievingNonExistentPortfolio() {
            // Arrange
            Long nonExistentId = 99L;
            when(portfolioRepository.findById(nonExistentId)).thenReturn(Optional.empty());        

            // Act & Assert
            assertThatThrownBy(() -> portfolioService.retrievePortfolioById(nonExistentId))
                .isInstanceOf(PortfolioNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(PortfolioNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });

            // Verify further interactions never occured
            verify(portfolioMapper, never()).toResponseDto(any(Portfolio.class));
        }
    }

    @Nested
    @DisplayName("retrieveAllPortfolios method")
    class RetrieveAllPortfolios {
        @Test
        @DisplayName("should return a list of all portfolios")
        void should_returnAllPortfolios() {
            // Arrange
            BrokerAccount brokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(brokerAccount, "createdAt", Instant.now());
            
            Instant createdAt = Instant.now();

            Long portfolioId1 = 1L;
            String portfolioName1 = "Test Portfolio 1";
            Portfolio portfolio1 = new Portfolio(portfolioName1, brokerAccount);
            ReflectionTestUtils.setField(portfolio1, "id", portfolioId1);
            ReflectionTestUtils.setField(portfolio1, "createdAt", createdAt);

            Long portfolioId2 = 2L;
            String portfolioName2 = "Test Portfolio 2";
            Portfolio portfolio2 = new Portfolio(portfolioName2, brokerAccount);
            ReflectionTestUtils.setField(portfolio2, "id", portfolioId2);
            ReflectionTestUtils.setField(portfolio2, "createdAt", createdAt);

            List<Portfolio> portfolioList = List.of(portfolio1, portfolio2);
            when(portfolioRepository.findAll()).thenReturn(portfolioList);

            var brokerDto = new PortfolioResponse.BrokerAccountInPortfolioResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            PortfolioResponse response1 = new PortfolioResponse(portfolioId1, portfolioName1, brokerDto, createdAt);
            PortfolioResponse response2 = new PortfolioResponse(portfolioId2, portfolioName2, brokerDto, createdAt);
            List<PortfolioResponse> responseList = List.of(response1, response2);
            when(portfolioMapper.toResponseDtoList(portfolioList)).thenReturn(responseList);  
            
            // Act
            List<PortfolioResponse> result = portfolioService.retrieveAllPortfolios();
            
            // Assert
            assertThat(result).isNotNull();
            assertThat(result).containsExactlyInAnyOrder(response1, response2);
            
            // Verify interactions
            verify(portfolioRepository).findAll();
            verify(portfolioMapper).toResponseDtoList(portfolioList);
        }
    }

    @Nested
    @DisplayName("updatePortfolio method")
    class UpdatePortfolio {
        @Test
        @DisplayName("should update portfolio when ID exists")
        void should_updatePortfolio_when_idExists() {
            // Arrange
            String updatedPortfolioName = "Updated Portfolio";
            Instant createdAt = Instant.now();
            
            BrokerAccount brokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(brokerAccount, "createdAt", createdAt);
            
            // Repository find the Entity by id
            Portfolio existingPortfolio = new Portfolio(PORTFOLIO_NAME, brokerAccount);
            ReflectionTestUtils.setField(existingPortfolio, "id", PORTFOLIO_ID);
            ReflectionTestUtils.setField(existingPortfolio, "createdAt", createdAt);
            when(portfolioRepository.findById(PORTFOLIO_ID)).thenReturn(Optional.of(existingPortfolio));

            // When save is called, we can just return the same instance that was passed to it
            when(portfolioRepository.save(any(Portfolio.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Mapper convert the updated entity to a response DTO
            var brokerDto = new PortfolioResponse.BrokerAccountInPortfolioResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
            PortfolioResponse response = new PortfolioResponse(PORTFOLIO_ID, updatedPortfolioName, brokerDto, createdAt);
            when(portfolioMapper.toResponseDto(existingPortfolio)).thenReturn(response);
            
            // Act
            UpdatePortfolioRequest request = new UpdatePortfolioRequest(updatedPortfolioName);
            PortfolioResponse result = portfolioService.updatePortfolio(PORTFOLIO_ID, request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<Portfolio> captor = ArgumentCaptor.forClass(Portfolio.class);
            verify(portfolioRepository).save(captor.capture());
            Portfolio capturedPortfolio = captor.getValue();
            
            assertThat(capturedPortfolio).isSameAs(existingPortfolio); // It's the exact same instance
            assertThat(capturedPortfolio.getId()).isEqualTo(PORTFOLIO_ID); // The ID should be preserved
            assertThat(capturedPortfolio.getName()).isEqualTo(updatedPortfolioName); // The name should be updated
            assertThat(capturedPortfolio.getCreatedAt()).isEqualTo(createdAt); // The original createdAt should be preserved

            // Verify interactions
            verify(portfolioRepository).findById(PORTFOLIO_ID);
            verify(portfolioMapper).toResponseDto(existingPortfolio);
        }

        @Test
        @DisplayName("should throw PortfolioNotFoundException when ID does not exist")
        void should_throwException_when_updatingNonExistentPortfolio() {
            // Arrange
            Long nonExistentId = 99L;

            when(portfolioRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> portfolioService.updatePortfolio(nonExistentId, any(UpdatePortfolioRequest.class)))
                .isInstanceOf(PortfolioNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(PortfolioNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });

            // Verify that no further interactions occurred
            verify(portfolioRepository, never()).save(any(Portfolio.class));
            verify(portfolioMapper, never()).toResponseDto(any(Portfolio.class));
        }
    }

    @Nested
    @DisplayName("deletePortfolioById method")
    class DeletePortfolio {
        @Test
        @DisplayName("should delete portfolio")
        void should_deletePortfolio() {
            // Act
            portfolioService.deletePortfolioById(PORTFOLIO_ID);

            // Assert
            verify(portfolioRepository).deleteById(PORTFOLIO_ID);
        }
    }
}
