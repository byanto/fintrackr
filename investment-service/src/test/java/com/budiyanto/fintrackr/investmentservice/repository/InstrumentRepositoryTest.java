package com.budiyanto.fintrackr.investmentservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.budiyanto.fintrackr.investmentservice.domain.Instrument;
import com.budiyanto.fintrackr.investmentservice.domain.InstrumentType;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InstrumentRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private final InstrumentRepository instrumentRepository;

    @Autowired
    InstrumentRepositoryTest(InstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    @Test
    void shouldSaveAndRetrieveInstrument() {
        // Arrange: Create a new Instrument object
        Instrument instrument = new Instrument();
        instrument.setName("Bank Central Asia");
        instrument.setCode("BBCA");
        instrument.setInstrumentType(InstrumentType.STOCK);
        instrument.setCurrency("IDR");

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
