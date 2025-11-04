package com.jugantar.RiskMod.controller;

import com.jugantar.RiskMod.dto.*;
import com.jugantar.RiskMod.service.AnalysisService;
import com.jugantar.RiskMod.service.AssetService;
import com.jugantar.RiskMod.service.FinancialDataService;
import com.jugantar.RiskMod.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication; // <-- IMPORTANT
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios") // Base URL for all portfolio endpoints
public class PortfolioController {

    private final PortfolioService portfolioService;
    private final AssetService assetService;
    private final FinancialDataService financialDataService;
    private final AnalysisService analysisService;

    @Autowired
    public PortfolioController(PortfolioService portfolioService, AssetService assetService, FinancialDataService financialDataService, AnalysisService analysisService) {
        this.portfolioService = portfolioService;
        this.assetService = assetService;
        this.financialDataService = financialDataService;
        this.analysisService = analysisService;
    }

    // This endpoint handles: POST /api/portfolios
    @PostMapping
    public ResponseEntity<PortfolioResponse> createPortfolio(
            @RequestBody PortfolioRequest portfolioRequest,
            Authentication authentication // <-- Spring injects this!
    ) {
        // 1. Get the username from the Authentication object.
        // Our JwtAuthenticationFilter is what makes this object available.
        String username = authentication.getName();

        // 2. Call the service to do the work
        PortfolioResponse response = portfolioService.createPortfolio(portfolioRequest, username);

        // 3. Return a 201 CREATED response with the new portfolio's details
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // This method handles: GET /api/portfolios
    @GetMapping
    public ResponseEntity<List<PortfolioResponse>> getPortfolios(
            Authentication authentication // <-- We get the user from the token
    ) {
        // 1. Get the username
        String username = authentication.getName();

        // 2. Call the service to get the list
        List<PortfolioResponse> portfolios = portfolioService.getPortfoliosByUsername(username);

        // 3. Return 200 OK with the list
        return ResponseEntity.ok(portfolios);
    }

    // --- NEW ENDPOINT TO ADD AN ASSET ---

    // This handles: POST /api/portfolios/{portfolioId}/assets
    @PostMapping("/{portfolioId}/assets")
    public ResponseEntity<AssetResponse> addAssetToPortfolio(
            @PathVariable Long portfolioId, // <-- Gets {portfolioId} from URL
            @RequestBody AssetRequest assetRequest,
            Authentication authentication
    ) {
        // 1. Get the username from the token
        String username = authentication.getName();

        // 2. Call the AssetService to do the work
        // The service contains the critical security check
        AssetResponse response = assetService.addAssetToPortfolio(
                portfolioId,
                assetRequest,
                username
        );

        // 3. Return 201 CREATED with the new asset's details
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // This handles: GET /api/portfolios/{portfolioId}/assets
    @GetMapping("/{portfolioId}/assets")
    public ResponseEntity<List<AssetResponse>> getAssetsForPortfolio(
            @PathVariable Long portfolioId, // <-- Gets {portfolioId} from URL
            Authentication authentication
    ) {
        // 1. Get the username from the token
        String username = authentication.getName();

        // 2. Call the AssetService to get the data
        // The service performs the security check
        List<AssetResponse> assets = assetService.getAssetsForPortfolio(portfolioId, username);

        // TEMPORARY TEST - DELETE THIS LATER
//        String testQuote = financialDataService.getStockQuote("IBM");
//        System.out.println("--- ALPHA VANTAGE TEST ---");
//        System.out.println(testQuote);
//        System.out.println("--------------------------");

        // 3. Return 200 OK with the list of assets
        return ResponseEntity.ok(assets);
    }

//    // This handles: GET /api/portfolios/{portfolioId}/performance
//    @GetMapping("/{portfolioId}/performance")
//    public ResponseEntity<PerformanceAnalysis> getPerformanceAnalysis(
//            @PathVariable Long portfolioId,
//            Authentication authentication
//    ) {
//        String username = authentication.getName();
//
//        // Call the new service
//        PerformanceAnalysis analysis = analysisService.calculatePerformance(portfolioId, username);
//
//        return ResponseEntity.ok(analysis);
//    }

    // This handles: GET /api/portfolios/{portfolioId}/analysis
    @GetMapping("/{portfolioId}/analysis")
    public ResponseEntity<?> getFullAnalysis(
            @PathVariable Long portfolioId,
            Authentication authentication
    ) {
        String username = authentication.getName();

        try {
            // Call the new, all-in-one service method
            FullAnalysis analysis = analysisService.getFullAnalysis(portfolioId, username);
            return ResponseEntity.ok(analysis);

        } catch (Exception e) {
            // Catch errors like "Portfolio has no assets" or API failures
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}