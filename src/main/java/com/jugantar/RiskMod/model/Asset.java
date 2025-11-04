package com.jugantar.RiskMod.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal; // Good for financial data

@Getter
@Setter
@Entity
@Table(name = "assets")
public class Asset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tickerSymbol;

    @Column(nullable = false)
    private BigDecimal quantity; // e.g., 10.5 shares

    @Column(nullable = false)
    private BigDecimal purchasePrice; // Price per share

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY) // Many assets can belong to one portfolio
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;
}