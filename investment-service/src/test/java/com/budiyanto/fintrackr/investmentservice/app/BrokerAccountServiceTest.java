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

import org.aspectj.util.Reflection;
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

import com.budiyanto.fintrackr.investmentservice.api.dto.BrokerAccountResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.CreateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.UpdateBrokerAccountRequest;
import com.budiyanto.fintrackr.investmentservice.app.exception.BrokerAccountNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.BrokerAccountMapper;
import com.budiyanto.fintrackr.investmentservice.domain.BrokerAccount;
import com.budiyanto.fintrackr.investmentservice.repository.BrokerAccountRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrokerService Tests")
class BrokerAccountServiceTest {

    @Mock
    private BrokerAccountRepository brokerAccountRepository;

    @Mock
    private BrokerAccountMapper brokerAccountMapper;

    @InjectMocks
    private BrokerAccountService brokerAccountService;

    private static final Long BROKER_ACCOUNT_ID = 1L;
    private static final String ACCOUNT_NAME = "Test Broker Account";
    private static final String BROKER_NAME = "Test Broker";

    @Nested
    @DisplayName("createBrokerAccount method")
    class CreateBrokerAccount {

        @Test
        @DisplayName("should create broker account")
        void should_createBrokerAccount() {
            // Arrange
            // Mapper converts request DTO to a transient entity (no ID yet)
            CreateBrokerAccountRequest request = new CreateBrokerAccountRequest(ACCOUNT_NAME, BROKER_NAME);
            BrokerAccount transientBrokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
            when(brokerAccountMapper.toBrokerAccount(request)).thenReturn(transientBrokerAccount);

            // Repository saves the transient entity and returns a persisted one (with an ID)
            Instant createdAt = Instant.now();
            BrokerAccount savedBrokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(savedBrokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(savedBrokerAccount, "createdAt", createdAt);
            when(brokerAccountRepository.save(transientBrokerAccount)).thenReturn(savedBrokerAccount);

            // Mapper converts the persisted entity to a response DTO
            BrokerAccountResponse response = new BrokerAccountResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME, createdAt);
            when(brokerAccountMapper.toResponseDto(savedBrokerAccount)).thenReturn(response);

            // Act
            BrokerAccountResponse result = brokerAccountService.createBrokerAccount(request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<BrokerAccount> captor = ArgumentCaptor.forClass(BrokerAccount.class);
            verify(brokerAccountRepository).save(captor.capture());
            BrokerAccount capturedBrokerAccount = captor.getValue();

            assertThat(capturedBrokerAccount).isSameAs(transientBrokerAccount); // It's the exact same instance
            assertThat(capturedBrokerAccount.getId()).isNull(); // Should be the transient entity
            assertThat(capturedBrokerAccount.getName()).isEqualTo(ACCOUNT_NAME);
            assertThat(capturedBrokerAccount.getBrokerName()).isEqualTo(BROKER_NAME);
            assertThat(capturedBrokerAccount.getCreatedAt()).isNull(); // Should be the transient entity

            // Verify interactions
            verify(brokerAccountMapper).toBrokerAccount(request);            
            verify(brokerAccountMapper).toResponseDto(savedBrokerAccount);  
        }
    }

    @Nested
    @DisplayName("retrieveBrokerAccountById method")
    class RetrieveBrokerAccountById {
        @Test
        @DisplayName("should return broker account when ID exists")
        void should_returnBrokerAccount_when_idExists() {
            // Arrange
            BrokerAccount brokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            when(brokerAccountRepository.findById(BROKER_ACCOUNT_ID)).thenReturn(Optional.of(brokerAccount));

            Instant createdAt = Instant.now();
            BrokerAccountResponse response = new BrokerAccountResponse(BROKER_ACCOUNT_ID, ACCOUNT_NAME, BROKER_NAME, createdAt);
            when(brokerAccountMapper.toResponseDto(brokerAccount)).thenReturn(response);

            // Act
            BrokerAccountResponse result = brokerAccountService.retrieveBrokerAccountById(BROKER_ACCOUNT_ID);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);
            assertThat(result.id()).isEqualTo(BROKER_ACCOUNT_ID);
            assertThat(result.name()).isEqualTo(ACCOUNT_NAME);
            assertThat(result.brokerName()).isEqualTo(BROKER_NAME);
            assertThat(result.createdAt()).isEqualTo(createdAt);

            // Verify interactions
            verify(brokerAccountRepository).findById(BROKER_ACCOUNT_ID);
            verify(brokerAccountMapper).toResponseDto(brokerAccount);
        }

        @Test
        @DisplayName("should throw exception when ID does not exist")
        void should_throwException_when_retrievingNonExistentBrokerAccount() {
            // Arrange
            Long nonExistendId = 99L;
            when(brokerAccountRepository.findById(nonExistendId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> brokerAccountService.retrieveBrokerAccountById(nonExistendId))
                .isInstanceOf(BrokerAccountNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(BrokerAccountNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistendId);
                });

            // Verify that no further interactions occurred
            verify(brokerAccountMapper, never()).toResponseDto(any(BrokerAccount.class));
            
        }
    }
    
    @Nested
    @DisplayName("retrieveAllBrokerAccounts method")
    class RetrieveAllBrokerAccounts {
        
        @Test
        @DisplayName("should return a list of all broker accounts")
        void should_returnAllBrokerAccounts() {
            // Arrange
            Long brokerAccountId1 = 1L;
            Long brokerAccountId2 = 2L;
            String brokerAccountName1 = "Test Broker Account 1";
            String brokerAccountName2 = "Test Broker Account 2";
            String brokerName1 = "Test Broker Name 1";
            String brokerName2 = "Test Broker Name 2";
            Instant createdAt1 = Instant.now();
            Instant createdAt2 = Instant.now();

            BrokerAccount brokerAccount1 = new BrokerAccount(brokerAccountName1, brokerName1);
            ReflectionTestUtils.setField(brokerAccount1, "id", brokerAccountId1);
            ReflectionTestUtils.setField(brokerAccount1, "createdAt", createdAt1);

            BrokerAccount brokerAccount2 = new BrokerAccount(brokerAccountName2, brokerName2);
            ReflectionTestUtils.setField(brokerAccount2, "id", brokerAccountId2);
            ReflectionTestUtils.setField(brokerAccount2, "createdAt", createdAt2);
            when(brokerAccountRepository.findAll()).thenReturn(List.of(brokerAccount1, brokerAccount2));

            BrokerAccountResponse response1 = new BrokerAccountResponse(brokerAccountId1, brokerAccountName1, brokerName1, createdAt1);
            BrokerAccountResponse response2 = new BrokerAccountResponse(brokerAccountId2, brokerAccountName2, brokerName2, createdAt2);
            when(brokerAccountMapper.toResponseDtoList(List.of(brokerAccount1, brokerAccount2))).thenReturn(List.of(response1, response2));

            // Act
            List<BrokerAccountResponse> result = brokerAccountService.retrieveAllBrokerAccounts();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).hasSize(2);
            assertThat(result).containsExactlyInAnyOrder(response1, response2);

            // Verify interactions
            verify(brokerAccountRepository).findAll();
            verify(brokerAccountMapper).toResponseDtoList(List.of(brokerAccount1, brokerAccount2));
        }
    }

    @Nested
    @DisplayName("updateBrokerAccount method")
    class UpdateBrokerAccount {
        @Test
        @DisplayName("should update broker account when ID exists")
        void should_updateBrokerAccount_when_idExists() {
            // Arrange
            String updatedAccountName = "Updated Account Name";
            String updatedBrokerName = "Updated Broker Name";

            Instant createdAt = Instant.now();
            BrokerAccount brokerAccount = new BrokerAccount(ACCOUNT_NAME, BROKER_NAME);
            ReflectionTestUtils.setField(brokerAccount, "id", BROKER_ACCOUNT_ID);
            ReflectionTestUtils.setField(brokerAccount, "createdAt", createdAt);
            when(brokerAccountRepository.findById(BROKER_ACCOUNT_ID)).thenReturn(Optional.of(brokerAccount));

            when(brokerAccountRepository.save(any(BrokerAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            BrokerAccountResponse response = new BrokerAccountResponse(BROKER_ACCOUNT_ID, updatedAccountName, updatedBrokerName, createdAt);
            when(brokerAccountMapper.toResponseDto(brokerAccount)).thenReturn(response);
                
            // Act
            UpdateBrokerAccountRequest request = new UpdateBrokerAccountRequest(updatedAccountName, updatedBrokerName);
            BrokerAccountResponse result = brokerAccountService.updateBrokerAccount(BROKER_ACCOUNT_ID, request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<BrokerAccount> captor = ArgumentCaptor.forClass(BrokerAccount.class);
            verify(brokerAccountRepository).save(captor.capture());
            BrokerAccount capturedBrokerAccount = captor.getValue();

            assertThat(capturedBrokerAccount).isSameAs(brokerAccount);
            assertThat(capturedBrokerAccount.getId()).isEqualTo(BROKER_ACCOUNT_ID);
            assertThat(capturedBrokerAccount.getName()).isEqualTo(updatedAccountName);
            assertThat(capturedBrokerAccount.getBrokerName()).isEqualTo(updatedBrokerName);
            assertThat(capturedBrokerAccount.getCreatedAt()).isEqualTo(createdAt);

            // Verify interactions
            verify(brokerAccountRepository).findById(BROKER_ACCOUNT_ID);
            verify(brokerAccountMapper).toResponseDto(brokerAccount);
        }

        @Test
        @DisplayName("should throw exception when ID does not exist")
        void should_throwException_when_updatingNonExistentBrokerAccount() {
            // Arrange
            Long nonExistentId = 99L;
            when(brokerAccountRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            
            // Act & Assert
            assertThatThrownBy(() -> brokerAccountService.updateBrokerAccount(nonExistentId, any(UpdateBrokerAccountRequest.class)))
                .isInstanceOf(BrokerAccountNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(BrokerAccountNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });

            // Verify that no further interactions occurred
            verify(brokerAccountRepository, never()).save(any(BrokerAccount.class));
            verify(brokerAccountMapper, never()).toResponseDto(any(BrokerAccount.class));
        }
    }

    @Nested
    @DisplayName("deleteBrokerAccountById method")
    class DeleteBrokerAccount {
        @Test
        @DisplayName("should delete broker account")
        void should_deleteBrokerAccount() {
            // Act
            brokerAccountService.deleteBrokerAccountById(BROKER_ACCOUNT_ID);

            // Assert
            verify(brokerAccountRepository).deleteById(BROKER_ACCOUNT_ID);
        }
    }
}
