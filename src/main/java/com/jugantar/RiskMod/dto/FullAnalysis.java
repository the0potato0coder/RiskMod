package com.jugantar.RiskMod.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FullAnalysis {

    // This object will hold:
    // totalOriginalCost, totalCurrentValue, etc.
    private PerformanceAnalysis performance;

    // This object will hold:
    // volatility, sharpeRatio
    private RiskAnalysis risk;
}
