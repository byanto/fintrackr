package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import com.budiyanto.fintrackr.portfolio.domain.model.Acquisition;
import com.budiyanto.fintrackr.portfolio.domain.model.AcquisitionId;
import com.budiyanto.fintrackr.portfolio.domain.model.Buy;
import com.budiyanto.fintrackr.portfolio.domain.model.Deposit;
import com.budiyanto.fintrackr.portfolio.domain.model.Portfolio;
import com.budiyanto.fintrackr.portfolio.domain.model.PortfolioId;
import com.budiyanto.fintrackr.portfolio.domain.model.Transaction;
import com.budiyanto.fintrackr.portfolio.domain.model.TransactionId;
import com.budiyanto.fintrackr.shared.AssetId;
import com.budiyanto.fintrackr.shared.BrokerAccountId;
import com.budiyanto.fintrackr.shared.Money;
import com.budiyanto.fintrackr.shared.Quantity;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class PortfolioPersistenceMapper {

    Portfolio toDomain(PortfolioJpaEntity entity) {
        PortfolioId portfolioId = PortfolioId.of(entity.getId());
        BrokerAccountId brokerAccountId = BrokerAccountId.of(entity.getBrokerAccountId());
        String name = entity.getName();
        Money tradingBalance = toMoney(entity.getTradingBalance());
        List<Transaction> transactions = new ArrayList<>();
        for (TransactionJpaEntity transactionEntity : entity.getTransactions()) {
            switch (transactionEntity) {
                case DepositJpaEntity deposit -> transactions.add(
                        Deposit.reconstitute(
                                TransactionId.of(deposit.getId()),
                                portfolioId,
                                deposit.getDate(),
                                toMoney(deposit.getAmount())
                        )
                );
                case BuyJpaEntity buy -> transactions.add(
                        Buy.reconstitute(
                                TransactionId.of(buy.getId()),
                                portfolioId,
                                buy.getDate(),
                                AssetId.of(buy.getAssetId()),
                                Quantity.reconstitute(buy.getQuantity()),
                                toMoney(buy.getPrice()),
                                toMoney(buy.getFee()),
                                AcquisitionId.of(buy.getAcquisitionId())
                        )
                );
                default -> throw new IllegalStateException("Unknown transaction type: " + transactionEntity.getClass());
            }
        }

        List<Acquisition> acquisitions = new ArrayList<>();
        for (AcquisitionJpaEntity acquisitionEntity : entity.getAcquisitions()) {
            acquisitions.add(Acquisition.reconstitute(
                    AcquisitionId.of(acquisitionEntity.getId()),
                    portfolioId,
                    AssetId.of(acquisitionEntity.getAssetId()),
                    acquisitionEntity.getOpenDate(),
                    toMoney(acquisitionEntity.getOpenPrice()),
                    toMoney(acquisitionEntity.getOpenFee()),
                    Quantity.reconstitute(acquisitionEntity.getInitialQuantity())
            ));
        }

        return Portfolio.reconstitute(portfolioId, brokerAccountId, name, tradingBalance, transactions, acquisitions);
    }

    PortfolioJpaEntity toEntity(Portfolio portfolio) {
        PortfolioJpaEntity entity = new PortfolioJpaEntity(portfolio.id().value(), portfolio.brokerAccountId().value());
        updateEntity(portfolio, entity);
        return entity;
    }

    void updateEntity(Portfolio domain, PortfolioJpaEntity entity) {
        entity.setName(domain.name());
        entity.setTradingBalance(toEmbeddable(domain.tradingBalance()));

        Set<UUID> transactionEntityIdSet = entity.getTransactions().stream()
                .map(TransactionJpaEntity::getId)
                .collect(Collectors.toCollection(HashSet::new));

        for (Transaction transaction : domain.transactions()) {
            if (!transactionEntityIdSet.contains(transaction.id().value())) {
                switch (transaction) {
                    case Deposit deposit -> entity.addTransaction(
                            new DepositJpaEntity(
                                    deposit.id().value(),
                                    deposit.date(),
                                    toEmbeddable(deposit.amount())
                            )
                    );
                    case Buy buy -> entity.addTransaction(
                            new BuyJpaEntity(
                                    buy.id().value(),
                                    buy.date(),
                                    buy.assetId().value(),
                                    buy.quantity().value(),
                                    toEmbeddable(buy.price()),
                                    toEmbeddable(buy.fee()),
                                    buy.acquisitionId().value()
                            )
                    );
                }
            }
        }

        Set<UUID> acquisitionEntityIdSet = entity.getAcquisitions().stream()
                .map(AcquisitionJpaEntity::getId)
                .collect(Collectors.toCollection(HashSet::new));

        for (Acquisition acquisition : domain.acquisitions()) {
            if (!acquisitionEntityIdSet.contains(acquisition.id().value())) {
                entity.addAcquisition(new AcquisitionJpaEntity(
                        acquisition.id().value(),
                        acquisition.assetId().value(),
                        acquisition.openDate(),
                        toEmbeddable(acquisition.openPrice()),
                        toEmbeddable(acquisition.openFee()),
                        acquisition.initialQuantity().value()
                ));
            }
        }
    }

    private Money toMoney(MoneyEmbeddable embeddable) {
        return Money.of(embeddable.getAmount(), Currency.getInstance(embeddable.getCurrency()));
    }

    private MoneyEmbeddable toEmbeddable(Money money) {
        return new MoneyEmbeddable(money.amount(), money.currency().getCurrencyCode());
    }

}
