package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

import com.budiyanto.fintrackr.investmentservice.api.dto.CreateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.api.dto.InstrumentResponse;
import com.budiyanto.fintrackr.investmentservice.api.dto.UpdateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.app.exception.InstrumentNotFoundException;
import com.budiyanto.fintrackr.investmentservice.app.mapper.InstrumentMapper;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.repository.InstrumentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("InstrumentService Tests")
class InstrumentServiceTest {

    @Mock
    private InstrumentRepository instrumentRepository;

    @Mock
    private InstrumentMapper instrumentMapper;

    @InjectMocks
    private InstrumentService instrumentService;

    private static final Long INSTRUMENT_ID = 1L;
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.STOCK;
    private static final String INSTRUMENT_CODE = "BBCA";
    private static final String INSTRUMENT_NAME = "Bank Central Asia";
    private static final String INSTRUMENT_CURRENCY = "IDR";

    @Nested
    @DisplayName("createInstrument method")
    class CreateInstrument {

        @Test
        @DisplayName("should create and return a new instrument")
        void should_createInstrument() {
            // Arrange
            Instant createdAt = Instant.now();
            CreateInstrumentRequest request = new CreateInstrumentRequest(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);

            // Mapper converts request DTO to a transient entity (no ID yet)
            Instrument transientInstrument = new Instrument(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            when(instrumentMapper.toInstrument(request)).thenReturn(transientInstrument);

            // Repository saves the transient entity and returns a persisted one (with an ID)
            Instrument savedInstrument = new Instrument(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            ReflectionTestUtils.setField(savedInstrument, "id", INSTRUMENT_ID);
            ReflectionTestUtils.setField(savedInstrument, "createdAt", createdAt);
            when(instrumentRepository.save(transientInstrument)).thenReturn(savedInstrument);
            
            // Mapper converts the persisted entity to a response DTO
            InstrumentResponse response = new InstrumentResponse(INSTRUMENT_ID, INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY, createdAt);
            when(instrumentMapper.toResponseDto(savedInstrument)).thenReturn(response);

            // Act
            InstrumentResponse result = instrumentService.createInstrument(request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<Instrument> captor = ArgumentCaptor.forClass(Instrument.class);
            verify(instrumentRepository, times(1)).save(captor.capture());
            Instrument capturedInstrument = captor.getValue();

            assertThat(capturedInstrument).isSameAs(transientInstrument);
            assertThat(capturedInstrument.getId()).isNull();
            assertThat(capturedInstrument.getInstrumentType()).isEqualTo(INSTRUMENT_TYPE);
            assertThat(capturedInstrument.getCode()).isEqualTo(INSTRUMENT_CODE);
            assertThat(capturedInstrument.getName()).isEqualTo(INSTRUMENT_NAME);
            assertThat(capturedInstrument.getCurrency()).isEqualTo(INSTRUMENT_CURRENCY);
            assertThat(capturedInstrument.getCreatedAt()).isNull();
        
        }   
    }

    @Nested
    @DisplayName("retrieveInstrumentById method")
    class RetrieveInstrumentById {
        
        @Test
        @DisplayName("should return instrument when ID exists")
        void should_returnInstrument_when_idExists() {
            // Arrange
            Instant createdAt = Instant.now();

            // Repository finds the instrument based on the given ID
            Instrument retrievedInstrument = new Instrument(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            ReflectionTestUtils.setField(retrievedInstrument, "id", INSTRUMENT_ID);
            ReflectionTestUtils.setField(retrievedInstrument, "createdAt", createdAt);
            when(instrumentRepository.findById(INSTRUMENT_ID)).thenReturn(Optional.of(retrievedInstrument));

            // Mapper converts the retrieved entity to a response DTO
            InstrumentResponse response = new InstrumentResponse(INSTRUMENT_ID, INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY, createdAt);
            when(instrumentMapper.toResponseDto(retrievedInstrument)).thenReturn(response);

            // Act
            InstrumentResponse result = instrumentService.retrieveInstrumentById(INSTRUMENT_ID);

            // Assert on the returned DTO
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Verify interactions
            verify(instrumentRepository, times(1)).findById(INSTRUMENT_ID);
            verify(instrumentMapper, times(1)).toResponseDto(retrievedInstrument);
        }

        @Test
        @DisplayName("should throw InstrumentNotFoundException when ID does not exist")
        void should_throwException_when_retrievingNonExistantInstrument() {
            // Arrange
            Long nonExistentId = 99L;

            // Repository cannot find the instrument based on the given ID, returns empty optional
            when(instrumentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> instrumentService.retrieveInstrumentById(nonExistentId))
                .isInstanceOf(InstrumentNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(InstrumentNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });
        }
    }

    @Nested
    @DisplayName("retrieveAllInstruments method")
    class RetrieveAllInstruments {

        @Test
        @DisplayName("should return a list of all instruments")
        void should_returnAllInstruments() {
            // Arrange
            Instant createdAt1 = Instant.now();

            Long id2 = 2L;
            InstrumentType type2 = InstrumentType.MUTUAL_FUND;
            String code2 = "TRIM";
            String name2 = "Trimegah Fixed Income Plan";
            String currency2 = "IDR";
            Instant createdAt2 = Instant.now();

            Instrument instrument1 = new Instrument(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            ReflectionTestUtils.setField(instrument1, "id", INSTRUMENT_ID);
            ReflectionTestUtils.setField(instrument1, "createdAt", createdAt1);

            Instrument instrument2 = new Instrument(type2, code2, name2, currency2);
            ReflectionTestUtils.setField(instrument2, "id", id2);
            ReflectionTestUtils.setField(instrument2, "createdAt", createdAt2);

            List<Instrument> instrumentList = List.of(instrument1, instrument2);
            when(instrumentRepository.findAll()).thenReturn(instrumentList);

            InstrumentResponse response1 = new InstrumentResponse(INSTRUMENT_ID, INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY, createdAt1);
            InstrumentResponse response2 = new InstrumentResponse(id2, type2, code2, name2, currency2, createdAt2);
            List<InstrumentResponse> responseList = List.of(response1, response2);
            when(instrumentMapper.toResponseDtoList(instrumentList)).thenReturn(responseList);

            // Act
            List<InstrumentResponse> result = instrumentService.retrieveAllInstruments();

            // Assert
            assertThat(result).isNotNull();
            assertThat(result).containsExactlyInAnyOrder(response1, response2);

            // Verify interactions
            verify(instrumentRepository, times(1)).findAll();
            verify(instrumentMapper, times(1)).toResponseDtoList(instrumentList);
        }
    }

    @Nested
    @DisplayName("updateInstrument method")
    class UpdateInstrument {

        @Test
        @DisplayName("should update instrument when ID exists")
        void should_updateInstrument_when_idExists() {
            // Arrange
            Instant createdAt = Instant.now();
            String updatedCode = "BBRI";
            String updatedName = "Bank Rakyat Indonesia";

            // Repository find the Entity by id
            Instrument existingInstrument = new Instrument(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            ReflectionTestUtils.setField(existingInstrument, "id", INSTRUMENT_ID);
            ReflectionTestUtils.setField(existingInstrument, "createdAt", createdAt); 
            when(instrumentRepository.findById(INSTRUMENT_ID)).thenReturn(Optional.of(existingInstrument));

            // When save is called, we can just return the same instance that was passed to it
            when(instrumentRepository.save(any(Instrument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

            // Mapper converts the updated entity to a response DTO
            InstrumentResponse response = new InstrumentResponse(INSTRUMENT_ID, INSTRUMENT_TYPE, updatedCode, updatedName, INSTRUMENT_CURRENCY, createdAt);
            when(instrumentMapper.toResponseDto(existingInstrument)).thenReturn(response);

            // Act
            UpdateInstrumentRequest request = new UpdateInstrumentRequest(updatedCode, updatedName);
            InstrumentResponse result =instrumentService.updateInstrument(INSTRUMENT_ID, request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(response);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<Instrument> captor = ArgumentCaptor.forClass(Instrument.class);
            verify(instrumentRepository, times(1)).save(captor.capture());
            Instrument capturedInstrument = captor.getValue();
            
            assertThat(capturedInstrument).isSameAs(existingInstrument); // It's the exact same instance
            assertThat(capturedInstrument.getId()).isEqualTo(INSTRUMENT_ID); // The ID should be preserved
            assertThat(capturedInstrument.getCode()).isEqualTo(updatedCode); // The code should be updated
            assertThat(capturedInstrument.getName()).isEqualTo(updatedName); // The name should be updated
            assertThat(capturedInstrument.getInstrumentType()).isEqualTo(INSTRUMENT_TYPE); // The original instrumentType should be preserved
            assertThat(capturedInstrument.getCurrency()).isEqualTo(INSTRUMENT_CURRENCY); // The original currency should be preserved
            assertThat(capturedInstrument.getCreatedAt()).isEqualTo(createdAt); // The original createdAt should be preserved

        }

        @Test
        @DisplayName("should throw InstrumentNotFoundException when ID does not exist")
        void should_throwException_when_updatingNonExistentInstrument() {
            // Arrange
            Long nonExistentId = 99L;

            when(instrumentRepository.findById(nonExistentId)).thenReturn(Optional.empty());
            UpdateInstrumentRequest request = new UpdateInstrumentRequest("BBCA", "Bank Central Asia");
            
            // Act & Assert
             assertThatThrownBy(() -> instrumentService.updateInstrument(nonExistentId, request))
                .isInstanceOf(InstrumentNotFoundException.class)
                .asInstanceOf(InstanceOfAssertFactories.type(InstrumentNotFoundException.class))
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(nonExistentId);
                });
            
            // Verify
            verify(instrumentRepository, never()).save(any(Instrument.class));
            
        }
    }

    @Nested
    @DisplayName("deleteInstrumentById method")
    class DeleteInstrument {

        @Test
        @DisplayName("should delete instrument")
        void should_deleteInstrument() {
            // Act
            instrumentService.deleteInstrumentById(INSTRUMENT_ID);

            // Assert
            verify(instrumentRepository, times(1)).deleteById(INSTRUMENT_ID);
        }
    }
}
