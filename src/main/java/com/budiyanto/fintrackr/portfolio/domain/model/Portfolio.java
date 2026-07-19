package com.budiyanto.fintrackr.portfolio.domain.model;

import com.budiyanto.fintrackr.portfolio.domain.exception.*;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;

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
    private final List<Acquisition> acquisitions;

    private Portfolio (BrokerAccountId brokerAccountId, String name) {
        this.id = PortfolioId.generate();
        this.brokerAccountId = brokerAccountId;
        this.name = name;
        this.tradingBalance = Money.zero();
        this.transactions = new ArrayList<>();
        this.acquisitions = new ArrayList<>();
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

    public Money recordBuy(AssetId assetId, Quantity quantity, Money price, Money fee, LocalDate date, LocalDate today) {
        Objects.requireNonNull(assetId, "assetId cannot be null");
        Objects.requireNonNull(quantity, "quantity cannot be null");
        Objects.requireNonNull(price, "price cannot be null");
        Objects.requireNonNull(fee, "fee cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(today, "today cannot be null");

        if (quantity.isZero()) {
            throw new ZeroQuantityException(quantity);
        }

        if (price.isZeroOrNegative()) {
            throw new NonPositivePriceException(price);
        }

        if (fee.isNegative()) {
            throw new NegativeFeeException(fee);
        }

        if (date.isAfter(today)) {
            throw new FutureDatedTransactionException(date, today);
        }

        Money costDelta = Money.of(quantity.value().multiply(price.amount()).add(fee.amount()), price.currency()).negate();

        Money endBalance = tradingBalance.add(costDelta);
        if (endBalance.isNegative()) {
            throw new InsufficientBalanceException(tradingBalance, costDelta.negate());
        }

        Acquisition acquisition = Acquisition.create(id, assetId, date, price, fee, quantity);
        acquisitions.add(acquisition);

        Transaction buy = Buy.create(TransactionId.generate(), id, date, assetId, quantity, price, fee, acquisition.id());
        transactions.add(buy);

        tradingBalance = endBalance;
        return costDelta;
    }

    public PortfolioId id() { return id; }

    public Money tradingBalance() { return tradingBalance; }

    public List<Transaction> transactions() { return List.copyOf(transactions); }

    public List<Acquisition> acquisitions() { return List.copyOf(acquisitions); }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Portfolio portfolio = (Portfolio) o;
        return Objects.equals(id, portfolio.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
