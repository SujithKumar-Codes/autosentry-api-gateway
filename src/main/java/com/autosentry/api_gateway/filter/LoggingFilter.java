package com.autosentry.api_gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered; // <-- NEW IMPORT
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class LoggingFilter implements GlobalFilter, Ordered { // <-- IMPLEMENT ORDERED

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().toString();

        log.info("➡️ INCOMING REQUEST: {} {}", method, path);

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // By using a fallback to 500, we prevent NullPointerExceptions if the response was forcefully dropped
            int statusCode = exchange.getResponse().getStatusCode() != null
                    ? exchange.getResponse().getStatusCode().value()
                    : 500;

            log.info("⬅️ OUTGOING RESPONSE: {} {} - Status: {}", method, path, statusCode);
        }));
    }

    // THE MAGIC STEP: Set the priority
    @Override
    public int getOrder() {
        return -1; // Highest priority. This filter will run FIRST before any routing or security checks.
    }
}