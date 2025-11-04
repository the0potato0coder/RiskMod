package com.jugantar.RiskMod.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

@Service
public class FinancialDataService {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    @Autowired
    public FinancialDataService(RestTemplate restTemplate,
                                @Value("${alphavantage.api.key}") String apiKey, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    /**
     * Fetches the current price and other quote data for a stock ticker.
     * @param symbol The stock ticker (e.g., "AAPL")
     * @return The raw JSON response from Alpha Vantage.
     */
    public String getStockQuote(String symbol) {
        // This is the Alpha Vantage API URL for a "GLOBAL_QUOTE"
        String apiUrl = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                + symbol
                + "&apikey="
                + apiKey;

        // Call the external API and get the response as a String
        String response = restTemplate.getForObject(apiUrl, String.class);

        // We'll return the raw JSON string for now.
        // In a real app, we'd map this to a DTO (e.g., GlobalQuoteDTO).
        return response;
    }

    // We will add more methods here later, like getHistoricalData()

    /**
     * Fetches the last 100 days of adjusted closing prices for a stock.
     * @param symbol The stock ticker (e.g., "AAPL")
     * @return A Map where Key = Date, Value = Adjusted Close Price
     */
    public Map<LocalDate, BigDecimal> getHistoricalPrices(String symbol) {

        // We use "TIME_SERIES_DAILY_ADJUSTED" and "compact" (for last 100 days)
        String apiUrl = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol="
                + symbol
                + "&outputsize=compact" // "compact" = 100 data points
                + "&apikey=" + apiKey;

        try {
            // 1. Fetch the raw JSON as a String
            String jsonResponse = restTemplate.getForObject(apiUrl, String.class);

            // 2. Parse the entire string into a generic Map
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, typeRef);

            // 3. Check for API limit error (a common issue)
            if (responseMap.containsKey("Note")) {
                System.err.println("Alpha Vantage API limit likely reached: " + responseMap.get("Note"));
                return new HashMap<>(); // Return empty map
            }

            // 4. Navigate into the "Time Series (Daily)" object
            @SuppressWarnings("unchecked") // Suppress warning for this manual cast
            Map<String, Map<String, String>> timeSeries =
                    (Map<String, Map<String, String>>) responseMap.get("Time Series (Daily)");

            if (timeSeries == null) {
                // This means the API call failed (rate limit, bad ticker, etc.)
                // Print the whole response so we can debug it
                System.err.println("Could not parse time series. Full response: " + jsonResponse);
                return new HashMap<>(); // Return empty map
            }

            // We use a TreeMap to sort the prices by date automatically
            Map<LocalDate, BigDecimal> prices = new TreeMap<>();

            // 5. Loop through each date entry
            for (Map.Entry<String, Map<String, String>> entry : timeSeries.entrySet()) {
                LocalDate date = LocalDate.parse(entry.getKey());

                // 6. Extract the "5. adjusted close" price
                BigDecimal price = new BigDecimal(entry.getValue().get("4. close"));

                prices.put(date, price);
            }

            return prices;

        } catch (Exception e) {
            System.err.println("Error parsing historical data for " + symbol + ": " + e.getMessage());
            return new HashMap<>(); // Return empty on error
        }
    }
}