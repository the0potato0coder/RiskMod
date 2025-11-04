package com.jugantar.RiskMod.repository;

import com.jugantar.RiskMod.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AssetRepository extends JpaRepository<Asset, Long> {

    // "SELECT * FROM assets WHERE portfolio_id = ?"
    List<Asset> findByPortfolioId(Long portfolioId);
}