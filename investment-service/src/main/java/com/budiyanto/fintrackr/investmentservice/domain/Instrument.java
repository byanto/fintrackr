package com.budiyanto.fintrackr.investmentservice.domain;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "instrument")
@Getter
@NoArgsConstructor (force = true)
public class Instrument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false, updatable = false)
    private final InstrumentType instrumentType;
    
    @Setter
    @Column(name = "code", nullable = false, unique = true) // Made mutable as requested
    private String code;
    
    @Setter
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "currency", nullable = false, updatable = false)
    private final String currency;

    @CreationTimestamp
    @Column(name = "created_at",nullable = false, updatable = false)
    private Instant createdAt;

    public Instrument(InstrumentType instrumentType, String code, String name, String currency) {
        this.instrumentType = instrumentType;
        this.code = code;
        this.name = name;
        this.currency = currency;
    }

}
