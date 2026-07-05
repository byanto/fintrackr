package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.portfolio.domain.exception.FutureDatedTransactionException;
import com.budiyanto.fintrackr.portfolio.domain.exception.NonPositiveAmountException;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Portfolio {

    private final PortfolioId id;
    private final BrokerAccountId brokerAccountId;
    private String name;
    private Money tradingBalance;
    private final List<Transaction> transactions;

    private Portfolio (BrokerAccountId brokerAccountId, String name) {
        this.id = PortfolioId.generate();
        this.brokerAccountId = brokerAccountId;
        this.name = name;
        this.tradingBalance = Money.zero();
        this.transactions = new ArrayList<>();
    }

    public static Portfolio create(BrokerAccountId brokerAccountId, String name) {
        return new Portfolio(brokerAccountId, name);
    }

    public void recordDeposit(Money amount, LocalDate date, LocalDate today) {
        Objects.requireNonNull(amount, "amount cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(today, "today cannot be null");

        if (amount.isZeroOrNegative()) {
            throw new NonPositiveAmountException(amount);
        }

        if (date.isAfter(today)) {
            throw new FutureDatedTransactionException(date, today);
        }

        Transaction deposit = Deposit.create(TransactionId.generate(), id, date, amount);
        transactions.add(deposit);

        tradingBalance = tradingBalance.add(amount);
    }

    public PortfolioId id() { return id; }

    public Money tradingBalance() { return tradingBalance; }

    public List<Transaction> transactions() { return List.copyOf(transactions); }
}
