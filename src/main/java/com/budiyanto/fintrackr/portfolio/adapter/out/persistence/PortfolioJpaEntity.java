package com.budiyanto.fintrackr.portfolio.adapter.out.persistence;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "portfolios")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter()
class PortfolioJpaEntity {

    @Version
    @Column(name = "version")
    private Long version;

    @Id
    @Column(name = "id")
    private UUID id;

    @Column(name = "broker_account_id")
    private UUID brokerAccountId;

    @Setter(AccessLevel.PACKAGE)
    @Column(name = "name")
    private String name;

    @Setter(AccessLevel.PACKAGE)
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "trading_balance_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "trading_balance_currency"))
    })
    private MoneyEmbeddable tradingBalance;

    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @OrderBy("date ASC, seq ASC")
    private List<TransactionJpaEntity> transactions;

    @Getter(AccessLevel.NONE)
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "portfolio_id", nullable = false)
    @OrderBy("openDate ASC, id ASC")
    private List<AcquisitionJpaEntity> acquisitions;

    PortfolioJpaEntity(UUID id, UUID brokerAccountId) {
        this.id = id;
        this.brokerAccountId = brokerAccountId;
        this.transactions = new ArrayList<>();
        this.acquisitions = new ArrayList<>();
    }

    void addTransaction(TransactionJpaEntity transaction) {
        Objects.requireNonNull(transaction, "transaction cannot be null");
        transactions.add(transaction);
    }

    void addAcquisition(AcquisitionJpaEntity acquisition) {
        Objects.requireNonNull(acquisition, "acquisition cannot be null");
        acquisitions.add(acquisition);
    }

    List<TransactionJpaEntity> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    List<AcquisitionJpaEntity> getAcquisitions() {
        return Collections.unmodifiableList(acquisitions);
    }

}
