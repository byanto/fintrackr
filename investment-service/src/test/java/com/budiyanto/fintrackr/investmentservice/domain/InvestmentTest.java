package com.budiyanto.fintrackr.investmentservice.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class InvestmentTest {

    @Test
    void givenValidData_whenCreatingInvestment_thenSucceed() {
        // Given
        var id = "123";
        var name = "Tesla Stock";
        var amount = new BigDecimal("1000.00");

        // When
        var investment = new Investment(id, name, amount);

        // Then
        assertThat(investment).isNotNull();
        assertThat(investment.id()).isEqualTo(id);
        assertThat(investment.name()).isEqualTo(name);
        assertThat(investment.amount()).isEqualTo(amount);
    }
}