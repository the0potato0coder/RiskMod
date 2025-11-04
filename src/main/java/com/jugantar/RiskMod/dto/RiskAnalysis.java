package com.jugantar.RiskMod.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskAnalysis {

    // Standard Deviation of daily returns (annualized)
    private Double volatility;

    // Risk-adjusted return
    private Double sharpeRatio;
}
