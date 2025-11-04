package com.jugantar.RiskMod.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PortfolioResponse {
    private Long id;
    private String name;
    private Long userId;

    public PortfolioResponse(Long id, String name, Long userId) {
        this.id = id;
        this.name = name;
        this.userId = userId;
    }
}
