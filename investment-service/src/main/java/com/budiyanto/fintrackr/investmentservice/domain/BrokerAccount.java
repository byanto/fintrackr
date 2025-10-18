package com.budiyanto.fintrackr.investmentservice.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "broker_account")
@Getter
@NoArgsConstructor
public class BrokerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Setter
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Setter
    @Column(name = "broker_name", nullable = false)
    private String brokerName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public BrokerAccount(String name, String brokerName) {
        this.name = name;
        this.brokerName = brokerName;
    }
    
}
