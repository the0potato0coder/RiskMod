package com.jugantar.RiskMod.service;

import com.jugantar.RiskMod.dto.AssetRequest;
import com.jugantar.RiskMod.dto.AssetResponse;
import com.jugantar.RiskMod.model.Asset;
import com.jugantar.RiskMod.model.Portfolio;
import com.jugantar.RiskMod.repository.AssetRepository;
import com.jugantar.RiskMod.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException; // <-- IMPORTANT
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class AssetService {

    private final AssetRepository assetRepository;
    private final PortfolioRepository portfolioRepository;

    @Autowired
    public AssetService(AssetRepository assetRepository, PortfolioRepository portfolioRepository) {
        this.assetRepository = assetRepository;
        this.portfolioRepository = portfolioRepository;
    }

    public AssetResponse addAssetToPortfolio(Long portfolioId, AssetRequest request, String username) {

        // 1. Find the portfolio
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        // 2. !!! SECURITY CHECK !!!
        // Check if the logged-in user (from the token) owns this portfolio
        if (!portfolio.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to add assets to this portfolio");
        }

        // 3. If the check passes, create the new asset
        Asset asset = new Asset();
        asset.setTickerSymbol(request.getTickerSymbol());
        asset.setQuantity(request.getQuantity());
        asset.setPurchasePrice(request.getPurchasePrice());

        // 4. Link the asset to the portfolio
        asset.setPortfolio(portfolio);

        // 5. Save the new asset
        Asset savedAsset = assetRepository.save(asset);

        // 6. Return the response DTO
        return new AssetResponse(
                savedAsset.getId(),
                savedAsset.getTickerSymbol(),
                savedAsset.getQuantity(),
                savedAsset.getPurchasePrice(),
                portfolio.getId()
        );
    }

    /**
     * Retrieves all assets for a specific portfolio, after verifying ownership.
     * @param portfolioId The ID of the portfolio to check.
     * @param username The username of the currently authenticated user.
     * @return A list of AssetResponse DTOs.
     */
    public List<AssetResponse> getAssetsForPortfolio(Long portfolioId, String username) {

        // 1. Find the portfolio
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));

        // 2. !!! SECURITY CHECK !!!
        // Verify that the logged-in user owns this portfolio
        if (!portfolio.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to view this portfolio");
        }

        // 3. If the check passes, find the assets
        // (We already created findByPortfolioId in our AssetRepository)
        List<Asset> assets = assetRepository.findByPortfolioId(portfolioId);

        // 4. Convert the list of Asset (Entities) to a list of AssetResponse (DTOs)
        return assets.stream()
                .map(asset -> new AssetResponse(
                        asset.getId(),
                        asset.getTickerSymbol(),
                        asset.getQuantity(),
                        asset.getPurchasePrice(),
                        portfolio.getId()
                ))
                .collect(Collectors.toList());
    }
}
