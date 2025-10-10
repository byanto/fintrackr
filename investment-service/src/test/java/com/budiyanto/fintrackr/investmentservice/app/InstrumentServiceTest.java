package com.budiyanto.fintrackr.investmentservice.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;

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

    @Nested
    @DisplayName("createInstrument method")
    class CreateInstrument {

        @Test
        @DisplayName("should create and return a new instrument")
        void should_createInstrument() {
            // Arrange
            Long id = 1L;
            InstrumentType type = InstrumentType.STOCK;
            String code = "BBCA";
            String name = "Bank Central Asia";
            String currency = "IDR";
            Instant createdAt = Instant.now();

            // Mapper converts request DTO to a transient entity (no ID yet)
            Instrument transientInstrument = new Instrument(type, code, name, currency);
            when(instrumentMapper.toInstrument(any(CreateInstrumentRequest.class))).thenReturn(transientInstrument);

            // Repository saves the transient entity and returns a persisted one (with an ID)
            Instrument savedInstrument = new Instrument(type, code, name, currency);
            ReflectionTestUtils.setField(savedInstrument, "id", id);
            ReflectionTestUtils.setField(savedInstrument, "createdAt", createdAt);
            when(instrumentRepository.save(any(Instrument.class))).thenReturn(savedInstrument);
            
            // Mapper converts the persisted entity to a response DTO
            InstrumentResponse response = new InstrumentResponse(id, type, code, name, currency, createdAt);
            when(instrumentMapper.toResponseDto(any(Instrument.class))).thenReturn(response);

            // Act
            CreateInstrumentRequest request = new CreateInstrumentRequest(type, code, name, currency);
            InstrumentResponse result = instrumentService.createInstrument(request);

            // Assert on the returned DTO (State-based test)
            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(id);
            assertThat(result.instrumentType()).isEqualTo(type);
            assertThat(result.code()).isEqualTo(code);
            assertThat(result.name()).isEqualTo(name);
            assertThat(result.currency()).isEqualTo(currency);
            assertThat(result.createdAt()).isEqualTo(createdAt);

            // Assert on the interaction: verify the correct entity was passed to save()
            ArgumentCaptor<Instrument> captor = ArgumentCaptor.forClass(Instrument.class);
            verify(instrumentRepository, times(1)).save(captor.capture());
            Instrument capturedInstrument = captor.getValue();

            assertThat(capturedInstrument).isSameAs(transientInstrument);
            assertThat(capturedInstrument.getId()).isNull();
            assertThat(capturedInstrument.getInstrumentType()).isEqualTo(type);
            assertThat(capturedInstrument.getCode()).isEqualTo(code);
            assertThat(capturedInstrument.getName()).isEqualTo(name);
            assertThat(capturedInstrument.getCurrency()).isEqualTo(currency);
            assertThat(capturedInstrument.getCreatedAt()).isNull();
        
        }   
    }

}
