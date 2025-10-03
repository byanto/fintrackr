package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;

class InstrumentRepositoryTest extends AbstractRepositoryTest{

    private final InstrumentRepository instrumentRepository;

    @Autowired
    InstrumentRepositoryTest(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Test
    void shouldSaveAndRetrieveInstrument() {
        // Arrange: Create a new Instrument object
        Instrument instrument = new Instrument(InstrumentType.STOCK, "BBCA", "Bank Central Asia", "IDR");

        // Act: Save the instrument using the repository
        Instrument savedInstrument = instrumentRepository.save(instrument);

        // Assert: Verify that the instrument was saved correctly and can be retrieved
        assertThat(savedInstrument).isNotNull();
        assertThat(savedInstrument.getId()).isGreaterThan(0);

        Instrument retrievedInstrument = instrumentRepository.findById(savedInstrument.getId()).orElse(null);
        assertThat(retrievedInstrument).isNotNull();
        assertThat(retrievedInstrument.getName()).isEqualTo(instrument.getName());
        assertThat(retrievedInstrument.getCode()).isEqualTo(instrument.getCode());
        assertThat(retrievedInstrument.getInstrumentType()).isEqualTo(instrument.getInstrumentType());
        assertThat(retrievedInstrument.getCurrency()).isEqualTo(instrument.getCurrency());
        assertThat(retrievedInstrument.getCreatedAt()).isNotNull(); 

    }

}
