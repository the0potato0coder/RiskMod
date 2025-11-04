package com.jugantar.RiskMod.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // This provides a single, reusable JSON parser for our app
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}