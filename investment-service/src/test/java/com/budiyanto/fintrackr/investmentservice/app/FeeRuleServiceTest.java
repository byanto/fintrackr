package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.budiyanto.fintrackr.investmentservice.api.dto.CreateFeeRuleRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.FeeRuleResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.UpdateFeeRuleRequest;
import com.budiyanto.fintrackr.investmentservice.app.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.exception.FeeRuleNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.FeeRuleMapper;
import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.domain.FeeRule;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.domain.TradeType;
import com.budiyanto.fintrackr.investmentservice.repository.BrokerAccountRepository;
import com.budiyanto.fintrackr.investmentservice.repository.FeeRuleRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeeRuleService Tests")
class FeeRuleServiceTest {

    @Mock
    private FeeRuleRepository feeRuleRepository;

    @Mock
    private BrokerAccountRepository brokerAccountRepository;

    @Mock
    private FeeRuleMapper feeRuleMapper;

    @InjectMocks
    private FeeRuleService feeRuleService;

    private static final Long FEE_RULE_ID = 20L;
    private static final Long BROKER_ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NAME = "Test Broker Account";
    private static final String BROKER_NAME = "Broker A";
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.STOCK;
    private static final TradeType TRADE_TYPE = TradeType.BUY;
    private static final BigDecimal FEE_PERCENTAGE = new BigDecimal("0.0018");
    private static final BigDecimal MIN_FEE = new BigDecimal("10000");

    private BrokerAccount brokerAccount;
    private FeeRuleResponse.BrokerAccountInFeeRuleResponse brokerAccountDto;


    @BeforeEach
    void setUp() {
        Instant createdAt = Instant.now();
        brokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
        ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
        ReflectionTestUtils.setField(brokerAccount, "createdAt", createdAt);

        brokerAccountDto = new FeeRuleResponse.BrokerAccountInFeeRuleResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME);
    }

    @Nested
    @DisplayName("createFeeRule method")
    class CreateFeeRule {
        @Test
        @DisplayName("should create and return a new fee rule")
        void should_createFeeRule_when_brokerAccountExists() {
            // Arrange
            CreateFeeRuleRequest request = new CreateFeeRuleRequest(BROKER_ACCOUNT_ID, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE);
            
            when(brokerAccountRepository.findById(request.brokerAccountId())).thenReturn(Optional.of(brokerAccount));

            FeeRule savedFeeRule = new FeeRule(brokerAccount, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE);
            when(feeRuleRepository.save(any(FeeRule.class))).thenReturn(savedFeeRule); // any() is fine here as we assert on the captor
            
            FeeRuleResponse response = new FeeRuleResponse(FEE_RULE_ID, brokerAccountDto, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE, Instant.now());
            when(feeRuleMapper.toResponseDto(savedFeeRule)).thenReturn(response);

            // Act
            FeeRuleResponse result = feeRuleService.createFeeRule(request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<FeeRule> captor = ArgumentCaptor.forClass(FeeRule.class);
            when(feeRuleRepository.save(captor.capture())).thenReturn(savedFeeRule);
            FeeRule capturedFeeRule = captor.getValue();

            assertThat(capturedFeeRule.getId()).isNull(); // Should be the transient entity
            assertThat(capturedFeeRule.getBrokerAccount()).isSameAs(brokerAccount);
            assertThat(capturedFeeRule.getInstrumentType()).isEqualTo(INSTRUMENT_TYPE);
            assertThat(capturedFeeRule.getTradeType()).isEqualTo(TRADE_TYPE);
            assertThat(capturedFeeRule.getFeePercentage()).isEqualTo(FEE_PERCENTAGE);
            assertThat(capturedFeeRule.getMinFee()).isEqualTo(MIN_FEE);
            assertThat(capturedFeeRule.getCreatedAt()).isNull(); 

            // Verify interactions
            verify(brokerAccountRepository).findById(BROKER_ACCOUNT_ID);
            verify(feeRuleMapper).toResponseDto(savedFeeRule);
        }

        @Test
        @DisplayName("should throw BrokerAccountNotFoundException when broker account does not exist")
        void should_throwException_when_brokerAccountDoesNotExist() {
            // Arrange
            Long nonExistentBrokerAccountId = 99L;
            CreateFeeRuleRequest request = new CreateFeeRuleRequest(nonExistentBrokerAccountId, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE);
            when(brokerAccountRepository.findById(request.brokerAccountId())).thenReturn(Optional.empty());

            // Act & Assert 
            assertThatThrownBy(() -> feeRuleService.createFeeRule(request))
                .isInstanceOf(BrokerAccountNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(BrokerAccountNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentBrokerAccountId);
                });

            // Verify further interactions never occured
            verify(feeRuleMapper, never()).toResponseDto(any(FeeRule.class));
        
        }
    }

    @Nested
    @DisplayName("retrieveFeeRuleById method")
    class RetrieveFeeRuleById {
        @Test
        @DisplayName("should return fee rule when ID exists")
        void should_returnFeeRule_when_idExists() {
            // Arrange
            Instant createdAt = Instant.now();
            FeeRule feeRule = new FeeRule(brokerAccount, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE);
            ReflectionTestUtils.setField(feeRule, "id", FEE_RULE_ID);
            ReflectionTestUtils.setField(feeRule, "createdAt", createdAt);
            when(feeRuleRepository.findById(FEE_RULE_ID)).thenReturn(Optional.of(feeRule));

            FeeRuleResponse response = new FeeRuleResponse(FEE_RULE_ID, brokerAccountDto, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE, createdAt);
            when(feeRuleMapper.toResponseDto(feeRule)).thenReturn(response);
            
            // Act
            FeeRuleResponse result = feeRuleService.retrieveFeeRuleById(FEE_RULE_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Verify interactions
            verify(feeRuleRepository).findById(FEE_RULE_ID);
            verify(feeRuleMapper).toResponseDto(feeRule);
        }

        @Test
        @DisplayName("should throw FeeRuleNotFoundException when ID does not exist")
        void should_throwException_when_retrievingNonExistentFeeRule() {
            // Arrange
            Long nonExistentId = 99L;
            when(feeRuleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> feeRuleService.retrieveFeeRuleById(nonExistentId))
                .isInstanceOf(FeeRuleNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(FeeRuleNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });

            // Verify further interactions never occured
            verify(feeRuleMapper, never()).toResponseDto(any(FeeRule.class));
        }
    }    
    
    @Nested
    @DisplayName("retrieveAllFeeRules method")
    class RetrieveAllFeeRules {
        @Test
        @DisplayName("should return a list of all fee rules")
        void should_returnAllFeeRules() {
            // Arrange
            Instant createdAt = Instant.now();

            Long feeRuleId1 = 1L;
            InstrumentType instrumentType1 = InstrumentType.STOCK;
            TradeType tradeType1 = TradeType.BUY;
            BigDecimal feePercentage1 = new BigDecimal("0.0018");
            BigDecimal minFee1 = new BigDecimal("10000");    
            
            Long feeRuleId2 = 2L;
            InstrumentType instrumentType2 = InstrumentType.BOND;
            TradeType tradeType2 = TradeType.SELL;
            BigDecimal feePercentage2 = new BigDecimal("0.0028");
            BigDecimal minFee2 = new BigDecimal("20000");  

            FeeRule feeRule1 = new FeeRule(brokerAccount, instrumentType1, tradeType1, feePercentage1, minFee1);
            FeeRule feeRule2 = new FeeRule(brokerAccount, instrumentType2, tradeType2, feePercentage2, minFee2);
            List<FeeRule> feeRuleList = List.of(feeRule1, feeRule2);
            when(feeRuleRepository.findAll()).thenReturn(feeRuleList);
            
            FeeRuleResponse response1 = new FeeRuleResponse(feeRuleId1, brokerAccountDto, instrumentType1, tradeType1, feePercentage1, minFee1, createdAt);
            FeeRuleResponse response2 = new FeeRuleResponse(feeRuleId2, brokerAccountDto, instrumentType2, tradeType2, feePercentage2, minFee2, createdAt);
            when(feeRuleMapper.toResponseDtoList(feeRuleList)).thenReturn(List.of(response1, response2));

            // Act
            List<FeeRuleResponse> result = feeRuleService.retrieveAllFeeRules();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).containsExactlyInAnyOrder(response1, response2);

            // Verify interactions
            verify(feeRuleRepository).findAll();
            verify(feeRuleMapper).toResponseDtoList(feeRuleList);

        }
    }

    @Nested
    @DisplayName("updateFeeRule method")
    class UpdateFeeRule {
        @Test
        @DisplayName("should update fee rule when ID exists")
        void should_updateFeeRule_when_idExists() {
            // Arrange
            Instant createdAt = Instant.now();

            // Repository find the Entity by id
            FeeRule feeRule = new FeeRule(brokerAccount, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE);
            ReflectionTestUtils.setField(feeRule, "id", FEE_RULE_ID);
            ReflectionTestUtils.setField(feeRule, "createdAt", createdAt);
            when(feeRuleRepository.findById(FEE_RULE_ID)).thenReturn(Optional.of(feeRule));

            // When save is called, we can just return the same instance that was passed to it
            when(feeRuleRepository.save(any(FeeRule.class)))
                .thenAnswer(invocation -> invocation.getArgument(0)); // any() is fine here as we assert on the captor

            FeeRuleResponse response = new FeeRuleResponse(FEE_RULE_ID, brokerAccountDto, INSTRUMENT_TYPE, TRADE_TYPE, FEE_PERCENTAGE, MIN_FEE, createdAt);
            when(feeRuleMapper.toResponseDto(feeRule)).thenReturn(response);

            // Act
            BigDecimal updatedFeePercentage = new BigDecimal("0.0028");
            BigDecimal updatedMinFee = new BigDecimal("30000");
            UpdateFeeRuleRequest request = new UpdateFeeRuleRequest(updatedFeePercentage, updatedMinFee);
            FeeRuleResponse result = feeRuleService.updateFeeRule(FEE_RULE_ID, request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<FeeRule> captor = ArgumentCaptor.forClass(FeeRule.class);
            verify(feeRuleRepository).save(captor.capture());
            FeeRule capturedFeeRule = captor.getValue();  

            assertThat(capturedFeeRule).isSameAs(feeRule); // It's the exact same instance
            assertThat(capturedFeeRule.getId()).isEqualTo(FEE_RULE_ID); // The ID should be preserved
            assertThat(capturedFeeRule.getBrokerAccount()).isEqualTo(brokerAccount); // The brokerAccount should be preserved
            assertThat(capturedFeeRule.getFeePercentage()).isEqualByComparingTo(updatedFeePercentage); // The feePercentage should be updated
            assertThat(capturedFeeRule.getMinFee()).isEqualByComparingTo(updatedMinFee);

            // Verify interactions
            verify(feeRuleRepository).findById(FEE_RULE_ID);
            verify(feeRuleMapper).toResponseDto(feeRule);
        }

        @Test
        @DisplayName("should throw FeeRuleNotFoundException when ID does not exist")
        void should_throwException_when_updatingNonExistentFeeRule() {
            // Arrange
            Long nonExistentId = 99L;
            when(feeRuleRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> feeRuleService.updateFeeRule(nonExistentId, any(UpdateFeeRuleRequest.class)))
                .isInstanceOf(FeeRuleNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(FeeRuleNotFoundException.class))
                .satisfies(ex  -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });
            
            // Verify that no further interactions occurred
            verify(feeRuleRepository, never()).save(any(FeeRule.class));
            verify(feeRuleMapper, never()).toResponseDto(any(FeeRule.class));
        }
    }

    @Nested
    @DisplayName("deleteFeeRule method")
    class DeleteFeeRule {
        @Test
        @DisplayName("should delete fee rule")
        void should_deleteFeeRule() {
            // Act
            feeRuleService.deleteFeeRule(FEE_RULE_ID);

            // Assert
            verify(feeRuleRepository).deleteById(FEE_RULE_ID);
        }
    }

}