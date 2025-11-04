package com.jugantar.RiskMod.dto;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class PerformanceAnalysis {

    private BigDecimal totalOriginalCost;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalGainLoss;
    private BigDecimal totalReturnPercentage;

    // We can add a list of individual asset performance later
}