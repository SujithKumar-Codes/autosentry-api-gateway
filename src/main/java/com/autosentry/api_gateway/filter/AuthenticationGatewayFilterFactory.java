package com.autosentry.api_gateway.filter;

import com.autosentry.api_gateway.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AuthenticationGatewayFilterFactory extends AbstractGatewayFilterFactory<AuthenticationGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public AuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            String path = exchange.getRequest().getURI().getPath();
            log.info("Incoming request intercepted by Gateway Auth Filter. Path: {}", path);

            // Safely extract the Authorization header in one step
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // If it's missing or isn't a Bearer token, reject it immediately
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("Unauthorized request attempt: Missing or malformed Authorization header for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            // Extract the actual JWT string
            String token = authHeader.substring(7);

            try {
                // Validate the token mathematically
                jwtUtil.validateToken(token);

                // Extract your specific claims
                String email = jwtUtil.extractEmail(token);
                String userId = jwtUtil.extractUserId(token);

                log.info("JWT token successfully validated. User Email: {}, User ID: {}", email, userId);

                // add custom headers
                ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                        .header("X-User-Email", email)
                        .header("X-User-Id", userId)
                        .build();

                log.debug("Injecting custom user headers into request context for path: {}", path);

                // Forward the modified request to the target microservice
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                // If token is expired or tampered throw exception
                log.error("Authentication failed: Invalid, tampered, or expired JWT token for path: {}. Error: {}", path, e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    public static class Config {
        // Required by AbstractGatewayFilterFactory
    }
}