package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.budiyanto.fintrackr.investmentservice.TestcontainersConfiguration;
import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
@DisplayName("InstrumentRepository Tests")
class InstrumentRepositoryTest {

    private final InstrumentRepository instrumentRepository;

    @Autowired
    InstrumentRepositoryTest(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Test
    @DisplayName("should save and retrieve instrument")
    void should_saveAndRetrieveInstrument() {
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
