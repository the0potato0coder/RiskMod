package com.jugantar.RiskMod.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "portfolios")
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // --- Relationships ---

    @ManyToOne(fetch = FetchType.LAZY) // Many portfolios can belong to one user
    @JoinColumn(name = "user_id", nullable = false) // This is the foreign key column
    private User user;
}