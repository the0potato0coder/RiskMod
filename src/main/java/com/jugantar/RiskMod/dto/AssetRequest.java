package com.jugantar.RiskMod.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal; // For financial data

@Getter
@Setter
public class AssetRequest {
    private String tickerSymbol;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
}
