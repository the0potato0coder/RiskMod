package com.jugantar.RiskMod.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore fields we don't care about
public class AlphaVantageQuote {

    // This maps the "Global Quote" JSON object to this class
    @JsonProperty("Global Quote")
    private GlobalQuote globalQuote;

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    // This tells Jackson to convert snake_case (like "01. symbol")
    // to camelCase (like "symbol")
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class GlobalQuote {

        // This maps "01. symbol" to this field
        @JsonProperty("01. symbol")
        private String symbol;

        // This maps "05. price" to this field
        @JsonProperty("05. price")
        private String price;
    }
}
