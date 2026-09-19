package com.budiyanto.fintrackr.brokerage.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.budiyanto.fintrackr.TestcontainersConfiguration;
import com.budiyanto.fintrackr.brokerage.domain.model.BrokerAccount;
import com.budiyanto.fintrackr.brokerage.domain.model.FeeStructure;
import com.budiyanto.fintrackr.brokerage.domain.model.Percentage;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@DataJpaTest
@Import({TestcontainersConfiguration.class, BrokerAccountPersistenceAdapter.class, BrokerAccountPersistenceMapper.class})
class BrokerAccountPersistenceAdapterTest {

    private final BrokerAccountPersistenceAdapter adapter;
    private final TestEntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    BrokerAccountPersistenceAdapterTest(BrokerAccountPersistenceAdapter adapter, TestEntityManager entityManager, JdbcTemplate jdbcTemplate) {
        this.adapter = adapter;
        this.entityManager = entityManager;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    @DisplayName("Write columns the schema expects when save")
    void should_writesColumnsTheSchemaExpects_when_save() {
        BrokerAccount savedAccount = savedAccount();

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT name, rdn_amount, rdn_currency, fee_structure_buy_rate, fee_structure_sell_rate FROM broker_accounts WHERE id = ?",
                savedAccount.id().value()
        );

        BigDecimal rdnAmount = (BigDecimal) row.get("rdn_amount");
        BigDecimal feeStructureBuyRate = (BigDecimal) row.get("fee_structure_buy_rate");
        BigDecimal feeStructureSellRate = (BigDecimal) row.get("fee_structure_sell_rate");

        assertThat(row.get("name")).isEqualTo("Test BrokerAccount");
        assertThat(rdnAmount).isEqualByComparingTo(new BigDecimal("50000"));
        assertThat(rdnAmount.scale()).isZero();
        assertThat(row.get("rdn_currency")).isEqualTo("IDR");
        assertThat(feeStructureBuyRate).isEqualByComparingTo(new BigDecimal("0.0015"));
        assertThat(feeStructureBuyRate.scale()).isEqualTo(6);
        assertThat(feeStructureSellRate).isEqualByComparingTo(new BigDecimal("0.0025"));
        assertThat(feeStructureSellRate.scale()).isEqualTo(6);
    }

    @Test
    @DisplayName("Reconstitute what was saved when findById")
    void should_reconstitutesWhatWasSaved_when_findById() {
        BrokerAccount savedAccount = savedAccount();

        BrokerAccount result = adapter.findById(savedAccount.id()).orElseThrow();
        assertThat(result.id()).isEqualTo(savedAccount.id());
        assertThat(result.rdn()).isEqualTo(savedAccount.rdn());
        assertThat(result.name()).isEqualTo(savedAccount.name());
        assertThat(result.feeStructure()).isEqualTo(savedAccount.feeStructure());
    }

    @Test
    @DisplayName("Return empty optional if id is unknown")
    void should_returnEmptyOptional_when_idIsUnknown() {
        // Given
        BrokerAccountId id = BrokerAccountId.generate();

        // When
        Optional<BrokerAccount> result = adapter.findById(id);

        // Then
        assertThat(result).isEmpty();
    }

    private BrokerAccount savedAccount() {
        FeeStructure feeStructure = FeeStructure.of(Percentage.of(new BigDecimal("0.0015")), Percentage.of(new BigDecimal("0.0025")));
        BrokerAccount brokerAccount = BrokerAccount.create("Test BrokerAccount", feeStructure);
        brokerAccount.applyCashFlow(Money.of(new BigDecimal("50000")));
        adapter.save(brokerAccount);

        entityManager.flush();
        entityManager.clear();

        return brokerAccount;
    }

}
