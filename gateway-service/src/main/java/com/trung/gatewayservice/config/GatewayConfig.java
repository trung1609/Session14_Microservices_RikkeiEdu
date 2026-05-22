package com.trung.gatewayservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteValidator routeValidator() {
        return new RouteValidator();
    }

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }

    @Bean
    public AuthenticationFilter authenticationFilter(RouteValidator routeValidator, JwtUtil jwtUtil) {
        return new AuthenticationFilter(routeValidator, jwtUtil);
    }
}

