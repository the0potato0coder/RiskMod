package com.jugantar.RiskMod.service;

import com.jugantar.RiskMod.dto.FullAnalysis;
import com.jugantar.RiskMod.dto.PerformanceAnalysis;
import com.jugantar.RiskMod.dto.RiskAnalysis;
import com.jugantar.RiskMod.model.Asset;
import com.jugantar.RiskMod.model.Portfolio;
import com.jugantar.RiskMod.repository.AssetRepository;
import com.jugantar.RiskMod.repository.PortfolioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private final PortfolioRepository portfolioRepository;
    private final AssetRepository assetRepository;
    private final FinancialDataService financialDataService;

    // A helper for our math
    private final StatisticsHelper statsHelper = new StatisticsHelper();

    // A hardcoded risk-free rate (e.g., 2% annualized)
    // We divide by 252 (trading days) for a daily rate.
    private static final double RISK_FREE_RATE = 0.02 / 252.0;

    @Autowired
    public AnalysisService(PortfolioRepository portfolioRepository,
                           AssetRepository assetRepository,
                           FinancialDataService financialDataService) {
        this.portfolioRepository = portfolioRepository;
        this.assetRepository = assetRepository;
        this.financialDataService = financialDataService;
    }

    /**
     * Calculates the full performance and risk analysis for a portfolio.
     */
    public FullAnalysis getFullAnalysis(Long portfolioId, String username) {

        // 1. Find portfolio & run security check
        Portfolio portfolio = portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> new RuntimeException("Portfolio not found"));
        if (!portfolio.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to analyze this portfolio");
        }

        List<Asset> assets = assetRepository.findByPortfolioId(portfolioId);
        if (assets.isEmpty()) {
            throw new RuntimeException("Portfolio has no assets to analyze.");
        }

        // --- Data Fetching ---
        // This map will store the historical data for ALL assets
        // Key: Ticker Symbol, Value: Map of (Date, Price)
        Map<String, Map<LocalDate, BigDecimal>> allAssetHistories = new HashMap<>();

        // We also need the *most recent* price for performance calculation
        Map<String, BigDecimal> mostRecentPrices = new HashMap<>();

        for (Asset asset : assets) {
            try {
                // Fetch the 100-day history for the asset
                Map<LocalDate, BigDecimal> history = financialDataService.getHistoricalPrices(asset.getTickerSymbol());

                if (history.isEmpty()) {
                    System.err.println("Could not get history for " + asset.getTickerSymbol());
                    continue; // Skip this asset
                }

                allAssetHistories.put(asset.getTickerSymbol(), history);

                // The history is a TreeMap, so it's sorted. Get the last (most recent) entry.
                LocalDate mostRecentDate = ((TreeMap<LocalDate, BigDecimal>) history).lastKey();
                mostRecentPrices.put(asset.getTickerSymbol(), history.get(mostRecentDate));

                // !!! IMPORTANT: AVOID API RATE LIMITS !!!
                // Alpha Vantage free tier is 25 calls/day. This sleep is critical.
                // In a real app, you'd cache data, but this works for a demo.
                Thread.sleep(1000); // 1 second sleep

            } catch (Exception e) {
                System.err.println("Error fetching data for " + asset.getTickerSymbol() + ": " + e.getMessage());
            }
        }

        // --- Calculations ---
        PerformanceAnalysis performance = calculatePerformance(assets, mostRecentPrices);
        RiskAnalysis risk = calculateRisk(assets, allAssetHistories);

        // --- Combine and Return ---
        FullAnalysis fullAnalysis = new FullAnalysis();
        fullAnalysis.setPerformance(performance);
        fullAnalysis.setRisk(risk);

        return fullAnalysis;
    }

    /**
     * Helper to calculate performance metrics.
     */
    private PerformanceAnalysis calculatePerformance(List<Asset> assets, Map<String, BigDecimal> currentPrices) {
        BigDecimal totalOriginalCost = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (Asset asset : assets) {
            BigDecimal originalCost = asset.getPurchasePrice().multiply(asset.getQuantity());
            totalOriginalCost = totalOriginalCost.add(originalCost);

            // Get the current price we fetched
            BigDecimal currentPrice = currentPrices.get(asset.getTickerSymbol());
            if (currentPrice != null) {
                BigDecimal currentValue = currentPrice.multiply(asset.getQuantity());
                totalCurrentValue = totalCurrentValue.add(currentValue);
            } else {
                // If API failed, just use original cost (no gain/loss)
                totalCurrentValue = totalCurrentValue.add(originalCost);
            }
        }

        PerformanceAnalysis analysis = new PerformanceAnalysis();
        analysis.setTotalOriginalCost(totalOriginalCost.setScale(2, RoundingMode.HALF_UP));
        analysis.setTotalCurrentValue(totalCurrentValue.setScale(2, RoundingMode.HALF_UP));

        BigDecimal gainLoss = totalCurrentValue.subtract(totalOriginalCost);
        analysis.setTotalGainLoss(gainLoss.setScale(2, RoundingMode.HALF_UP));

        if (totalOriginalCost.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal returnPercent = gainLoss.divide(totalOriginalCost, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal(100));
            analysis.setTotalReturnPercentage(returnPercent.setScale(2, RoundingMode.HALF_UP));
        } else {
            analysis.setTotalReturnPercentage(BigDecimal.ZERO);
        }
        return analysis;
    }

    /**
     * Helper to calculate risk metrics (Volatility and Sharpe Ratio).
     */
    private RiskAnalysis calculateRisk(List<Asset> assets, Map<String, Map<LocalDate, BigDecimal>> allAssetHistories) {

        // 1. Create Portfolio's 100-day value history
        // We use a TreeMap to keep it sorted by date
        Map<LocalDate, BigDecimal> portfolioHistory = new TreeMap<>();

        // Get a common list of dates (from the first asset)
        // This assumes all assets have the same 100 days of data
        List<LocalDate> dates = new ArrayList<>(allAssetHistories.values().iterator().next().keySet());

        for (LocalDate date : dates) {
            BigDecimal dailyPortfolioValue = BigDecimal.ZERO;
            for (Asset asset : assets) {
                Map<LocalDate, BigDecimal> assetHistory = allAssetHistories.get(asset.getTickerSymbol());
                if (assetHistory != null && assetHistory.containsKey(date)) {
                    BigDecimal price = assetHistory.get(date);
                    BigDecimal value = price.multiply(asset.getQuantity());
                    dailyPortfolioValue = dailyPortfolioValue.add(value);
                }
            }
            portfolioHistory.put(date, dailyPortfolioValue);
        }

        // 2. Calculate daily returns from the portfolio's history
        List<Double> dailyReturns = getDoubles(portfolioHistory);

        if (dailyReturns.isEmpty()) {
            return new RiskAnalysis(); // Not enough data
        }

        // 3. Calculate Volatility (Standard Deviation of daily returns)
        double volatility = statsHelper.standardDeviation(dailyReturns);

        // Annualize it (multiply by sqrt of 252 trading days)
        double annualizedVolatility = volatility * Math.sqrt(252);

        // 4. Calculate Sharpe Ratio
        double averageDailyReturn = statsHelper.mean(dailyReturns);

        // Daily Sharpe Ratio = (Avg Daily Return - Risk Free Rate) / Volatility
        double sharpeRatio = (averageDailyReturn - RISK_FREE_RATE) / volatility;

        // Annualize it (multiply by sqrt of 252)
        double annualizedSharpeRatio = sharpeRatio * Math.sqrt(252);

        // 5. Create response
        RiskAnalysis risk = new RiskAnalysis();
        // Return as a percentage
        risk.setVolatility(annualizedVolatility * 100);
        risk.setSharpeRatio(annualizedSharpeRatio);

        return risk;
    }

    private static List<Double> getDoubles(Map<LocalDate, BigDecimal> portfolioHistory) {
        List<Double> dailyReturns = new ArrayList<>();
        BigDecimal previousValue = null;
        for (BigDecimal value : portfolioHistory.values()) {
            if (previousValue != null && previousValue.compareTo(BigDecimal.ZERO) != 0) {
                // (current - previous) / previous
                BigDecimal change = value.subtract(previousValue);
                double dailyReturn = change.divide(previousValue, 6, RoundingMode.HALF_UP).doubleValue();
                dailyReturns.add(dailyReturn);
            }
            previousValue = value;
        }
        return dailyReturns;
    }

    /**
     * A simple helper class for calculating statistics.
     */
    private static class StatisticsHelper {

        public double mean(List<Double> data) {
            return data.stream()
                    .mapToDouble(d -> d)
                    .average()
                    .orElse(0.0);
        }

        public double standardDeviation(List<Double> data) {
            double mean = mean(data);
            double variance = data.stream()
                    .mapToDouble(d -> d)
                    .map(d -> Math.pow(d - mean, 2))
                    .average()
                    .orElse(0.0);
            return Math.sqrt(variance);
        }
    }
}