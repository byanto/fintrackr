package com.budiyanto.fintrackr.portfolio.adapter.in.web;

import com.budiyanto.fintrackr.portfolio.domain.model.Buy;
import com.budiyanto.fintrackr.portfolio.domain.model.Deposit;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
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

    static TransactionResponse from(Transaction transaction) {
        return switch (transaction) {
            case Deposit d -> new DepositResponse(d.id().value(), d.portfolioId().value(), d.date(), d.amount().amount());
            case Buy b -> new BuyResponse(b.id().value(), b.portfolioId().value(), b.date(), b.assetId().value(),
                    b.quantity().value(), b.price().amount(), b.fee().amount());
        };
    }

}
