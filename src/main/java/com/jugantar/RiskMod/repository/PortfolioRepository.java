package com.jugantar.RiskMod.repository;

import com.jugantar.RiskMod.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // "SELECT * FROM portfolios WHERE user_id = ?"
    List<Portfolio> findByUserId(Long userId);
}