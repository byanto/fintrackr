package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import com.budiyanto.fintrackr.TestcontainersConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({TestcontainersConfiguration.class})
class PortfolioPersistenceAdapterTest {

    @DisplayName("Write columns the schema expects when save")
    void should_writesColumnsTheSchemaExpects_when_save() {

    }

    @DisplayName("Return empty optional if id is unknown")
    void should_returnEmptyOptional_when_idIsUnknown() {

    }

    @DisplayName("Reconstitute what was saved when findById")
    void should_reconstitutesWhatWasSaved_when_findById() {

    }

    @DisplayName("Reconstitute every transaction type when findById")
    void should_reconstitutesEveryTransactionType_when_findById() {

    }

    @DisplayName("Keep a buy linked to its acquisition when findById")
    void should_keepsBuyLinkedToItsAcquisition_when_findById() {

    }

    @DisplayName("Persist the changed trading balance when an existing portfolio is saved")
    void should_persistChangedTradingBalance_when_existingPortfolioIsSaved() {

    }

    @DisplayName("Append only new ledger rows when an existing portfolio is saved")
    void should_appendsOnlyNewLedgerRows_when_existingPortfolioIsSaved() {

    }

    @DisplayName("Reject save when the broker account does not exist")
    void should_rejectSave_when_brokerAccountDoesNotExist() {

    }
    //@ParameterizedTest: ofShares(100) und ofUnits(100)
    @DisplayName("Keep share and unit quantities apart when findById")
    void should_keepsQuantityKindApart_when_findById() {

    }

    @DisplayName("Reject a type row when the base row has another type")
    void should_rejectTypeRow_when_baseRowHasOtherType() {

    }

    @DisplayName("Return ledger in recording order when find by id")
    void should_returnLedgerInRecordingOrder_when_findById() {

    }

}
