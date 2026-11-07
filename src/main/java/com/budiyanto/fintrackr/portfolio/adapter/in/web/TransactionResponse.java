package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.time.LocalDate;
import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DepositResponse.class, name = "DEPOSIT"),
        @JsonSubTypes.Type(value = BuyResponse.class, name = "BUY")
})
sealed interface TransactionResponse permits BuyResponse, DepositResponse {
    UUID id();
    UUID portfolioId();
    LocalDate date();
}
