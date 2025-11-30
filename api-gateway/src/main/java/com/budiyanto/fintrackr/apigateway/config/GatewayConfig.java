package com.budiyanto.fintrackr.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service", 
                            r -> r.path("/api/users/**", "/api/auth/**")                            
                            .uri("lb://user-service"))
                .route("investment-service", 
                            r -> r.path("/api/investments/**")                            
                            .uri("lb://investment-service"))
                .build();
    }
}
