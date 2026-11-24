package com.cedarmeadowmeats.orderworkflow.createcustomerinsquare.config;

import com.squareup.square.SquareClient;
import com.squareup.square.core.Environment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SquareConfig {

    public static final String SQUARE_ENVIRONMENT_PRODUCTION = "production";

    @Value("${create-customer-in-square.square.access-token:}")
    private String squareAccessKey;

    @Value("${create-customer-in-square.square.environment:}")
    private String environment;

    @Bean
    public SquareClient squareClient() {
        Environment squareEnvironment;

        if (SQUARE_ENVIRONMENT_PRODUCTION.equalsIgnoreCase(environment)) {
            squareEnvironment = Environment.PRODUCTION;
        } else {
            squareEnvironment = Environment.SANDBOX;
        }

        return SquareClient.builder()
                .environment(squareEnvironment)
                .token(squareAccessKey)
                .build();
    }
}
