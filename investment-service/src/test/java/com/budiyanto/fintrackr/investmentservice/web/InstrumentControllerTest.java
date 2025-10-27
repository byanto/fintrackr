package com.budiyanto.fintrackr.investmentservice.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;
import com.budiyanto.fintrackr.investmentservice.dto.CreateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.dto.InstrumentResponse;
import com.budiyanto.fintrackr.investmentservice.dto.UpdateInstrumentRequest;
import com.budiyanto.fintrackr.investmentservice.exception.InstrumentNotFoundException;
import com.budiyanto.fintrackr.investmentservice.service.InstrumentService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(InstrumentController.class)
@DisplayName("InstrumentController Tests")
class InstrumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InstrumentService instrumentService;

    private static final Long INSTRUMENT_ID = 1L;
    private static final InstrumentType INSTRUMENT_TYPE = InstrumentType.STOCK;
    private static final String INSTRUMENT_CODE = "BBCA";
    private static final String INSTRUMENT_NAME = "Bank Central Asia";
    private static final String INSTRUMENT_CURRENCY = "IDR";

    @Nested
    @DisplayName("POST /api/instruments")
    class CreateInstrument {
        @Test
        @DisplayName("should create instrument")
        void should_createInstrument_when_requestIsValid() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            CreateInstrumentRequest request = new CreateInstrumentRequest(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY);
            InstrumentResponse response = new InstrumentResponse(INSTRUMENT_ID, INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY, createdAt);
    
            when(instrumentService.createInstrument(request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(post("/api/instruments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(INSTRUMENT_ID))
                    .andExpect(jsonPath("$.instrumentType").value(INSTRUMENT_TYPE.toString()))
                    .andExpect(jsonPath("$.code").value(INSTRUMENT_CODE))
                    .andExpect(jsonPath("$.name").value(INSTRUMENT_NAME))
                    .andExpect(jsonPath("$.currency").value(INSTRUMENT_CURRENCY))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify that the service method was called with the correct argument
            verify(instrumentService).createInstrument(request);
        }

        @ParameterizedTest
        @MethodSource("provideInvalidCreateInstrumentRequests")
        @DisplayName("should return BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(CreateInstrumentRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(post("/api/instruments")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest))
                            )
                    .andExpect(status().isBadRequest());
        }

        private static Stream<CreateInstrumentRequest> provideInvalidCreateInstrumentRequests() {
            return Stream.of(
                // Null fields
                new CreateInstrumentRequest(null, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY),
                new CreateInstrumentRequest(INSTRUMENT_TYPE, "   ", INSTRUMENT_NAME, INSTRUMENT_CURRENCY),
                new CreateInstrumentRequest(INSTRUMENT_TYPE, INSTRUMENT_CODE, "   ", INSTRUMENT_CURRENCY),
                new CreateInstrumentRequest(INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, "   ")
            );
        }
    }
   
    @Nested
    @DisplayName("GET /api/instruments/{id}")
    class RetrieveInstrumentById {
        @Test
        @DisplayName("should return instrument when ID exists")
        void should_returnInstrument_whenIdExists() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();

            InstrumentResponse retrievedInstrument = new InstrumentResponse(INSTRUMENT_ID, INSTRUMENT_TYPE, INSTRUMENT_CODE, INSTRUMENT_NAME, INSTRUMENT_CURRENCY, createdAt);
            when(instrumentService.retrieveInstrumentById(INSTRUMENT_ID)).thenReturn(retrievedInstrument);

            // Act & Assert
            mockMvc.perform(get("/api/instruments/{id}", INSTRUMENT_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(INSTRUMENT_ID))
                    .andExpect(jsonPath("$.instrumentType").value(INSTRUMENT_TYPE.toString()))
                    .andExpect(jsonPath("$.code").value(INSTRUMENT_CODE))
                    .andExpect(jsonPath("$.name").value(INSTRUMENT_NAME))
                    .andExpect(jsonPath("$.currency").value(INSTRUMENT_CURRENCY))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify that the service method was called with the correct argument
            verify(instrumentService).retrieveInstrumentById(INSTRUMENT_ID);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_retrievingNonExistentInstrument() throws Exception {
            // Arrange
            Long nonExistentId = 99L;

            when(instrumentService.retrieveInstrumentById(nonExistentId))
                .thenThrow(new InstrumentNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(get("/api/instruments/{id}", nonExistentId))
                    .andExpect(status().isNotFound());

        }
    }

    @Nested
    @DisplayName("GET /api/instruments")
    class RetrieveAllInstruments {
        @Test
        @DisplayName("should return a list of all instruments")
        void should_returnAllInstruments() throws Exception {
            // Arrange
            Long id1 = 1L;
            Long id2 = 2L;
            String code1 = "BBCA";
            String code2 = "BBRI";
            String name1 = "Bank Central Asia";
            String name2 = "Bank Rakyat Indonesia";
            InstrumentResponse response1 = new InstrumentResponse(id1, INSTRUMENT_TYPE, code1, name1, INSTRUMENT_CURRENCY, Instant.now());
            InstrumentResponse response2 = new InstrumentResponse(id2, INSTRUMENT_TYPE, code2, name2, INSTRUMENT_CURRENCY, Instant.now());

            when(instrumentService.retrieveAllInstruments()).thenReturn(List.of(response1, response2));

            // Act & Assert
            mockMvc.perform(get("/api/instruments"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(2))
                    .andExpect(jsonPath("$[0].id").value(id1))
                    .andExpect(jsonPath("$[0].code").value(code1))
                    .andExpect(jsonPath("$[0].name").value(name1))
                    .andExpect(jsonPath("$[1].id").value(id2))
                    .andExpect(jsonPath("$[1].code").value(code2))
                    .andExpect(jsonPath("$[1].name").value(name2));

            // Verify that the service method was called with the correct argument
            verify(instrumentService).retrieveAllInstruments();
        }
    }

    @Nested
    @DisplayName("PUT /api/instruments/{id}")
    class UpdateInstrument {
        @Test
        @DisplayName("should update instrument when ID exists")
        void should_updateInstrument_when_idExists() throws Exception {
            // Arrange
            Instant createdAt = Instant.now();
            String updatedCode = "BBRI";
            String updatedName = "Bank Rakyat Indonesia";

            UpdateInstrumentRequest request = new UpdateInstrumentRequest(updatedCode, updatedName);
            InstrumentResponse response = new InstrumentResponse(INSTRUMENT_ID, INSTRUMENT_TYPE, updatedCode, updatedName, INSTRUMENT_CURRENCY, createdAt);
            when(instrumentService.updateInstrument(INSTRUMENT_ID, request)).thenReturn(response);

            // Act & Assert
            mockMvc.perform(put("/api/instruments/{id}", INSTRUMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(INSTRUMENT_ID))
                    .andExpect(jsonPath("$.instrumentType").value(INSTRUMENT_TYPE.toString()))
                    .andExpect(jsonPath("$.code").value(updatedCode))
                    .andExpect(jsonPath("$.name").value(updatedName))
                    .andExpect(jsonPath("$.currency").value(INSTRUMENT_CURRENCY))
                    .andExpect(jsonPath("$.createdAt").value(createdAt.toString()));

            // Verify that the service method was called with the correct argument 
            verify(instrumentService).updateInstrument(INSTRUMENT_ID, request);
        }

        @Test
        @DisplayName("should return NotFound when ID does not exist")
        void should_returnNotFound_when_updatingNonExistentInstrument() throws Exception {
            // Arrange
            Long nonExistentId = 99L;
            UpdateInstrumentRequest request = new UpdateInstrumentRequest("BBRI", "Bank Republik Indonesia");

            // Mock the service to throw the expected exception when called
            when(instrumentService.updateInstrument(eq(nonExistentId), any(UpdateInstrumentRequest.class)))
                .thenThrow(new InstrumentNotFoundException(nonExistentId));

            // Act & Assert
            mockMvc.perform(put("/api/instruments/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request))
                            )
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidUpdateInstrumentRequests")
        @DisplayName("should return BadRequest when any field is invalid")
        void should_returnBadRequest_when_requestIsInvalid(UpdateInstrumentRequest invalidRequest) throws Exception {
            // Act & Assert for invalid requests
            mockMvc.perform(put("/api/instruments/{id}", INSTRUMENT_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest))
                            )
                    .andExpect(status().isBadRequest());
        }

        private static Stream<UpdateInstrumentRequest> provideInvalidUpdateInstrumentRequests() {
            return Stream.of(
                new UpdateInstrumentRequest("   ", INSTRUMENT_NAME),
                new UpdateInstrumentRequest(INSTRUMENT_CODE, "   ")                
            );
        }
    }

    @Nested
    @DisplayName("DELETE /api/instruments/{id}")
    class DeleteInstrument {
        @Test
        @DisplayName("should delete instrument")
        void should_deleteInstrument() throws Exception {
            // Arrange
            doNothing().when(instrumentService).deleteInstrumentById(INSTRUMENT_ID);

            // Act & Assert
            mockMvc.perform(delete("/api/instruments/{id}", INSTRUMENT_ID))
                    .andExpect(status().isNoContent());
            
            // Verify that the service method was called with the correct argument
            verify(instrumentService).deleteInstrumentById(INSTRUMENT_ID);
        }
    }
}
