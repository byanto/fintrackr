package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.shared.Money;

import java.time.LocalDate;
import java.util.Objects;

public record Deposit(TransactionId id, PortfolioId portfolioId, LocalDate date, Money amount) implements Transaction {

    public Deposit {
        Objects.requireNonNull(id, "id cannot be null");
        Objects.requireNonNull(portfolioId, "portfolioId cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(amount, "amount cannot be null");
    }

    public static Deposit create(TransactionId id, PortfolioId portfolioId, LocalDate date, Money amount) {
        return new Deposit(id, portfolioId, date, amount);
    }

}
