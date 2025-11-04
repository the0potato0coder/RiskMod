package com.jugantar.RiskMod.dto;


import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class AssetResponse {
    private Long id;
    private String tickerSymbol;
    private BigDecimal quantity;
    private BigDecimal purchasePrice;
    private Long portfolioId;

    public AssetResponse(Long id, String tickerSymbol, BigDecimal quantity, BigDecimal purchasePrice, Long portfolioId) {
        this.id = id;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.purchasePrice = purchasePrice;
        this.portfolioId = portfolioId;
    }
}
