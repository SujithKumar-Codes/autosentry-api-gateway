package com.autosentry.api_gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();

        // Trust the Angular dev server
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:4200"));

        // Allow these HTTP methods
        corsConfig.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // Allow all headers (including Authorization/JWT)
        corsConfig.setAllowedHeaders(Arrays.asList("*"));

        // For tokens and cookies
        corsConfig.setAllowCredentials(true);

        // Cache the preflight request for 1 hour to speed up the frontend
        corsConfig.setMaxAge(3600L);

        // Apply this config to all routes
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }
}
