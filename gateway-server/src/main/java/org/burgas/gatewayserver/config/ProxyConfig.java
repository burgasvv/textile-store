package org.burgas.gatewayserver.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProxyConfig {

    @Bean
    public RouteLocator routeLocator(final RouteLocatorBuilder routeLocatorBuilder) {
        return routeLocatorBuilder.routes()
                .route(
                        "backend-server",
                        predicateSpec -> predicateSpec
                                .path(
                                        "/bills/**", "/buckets/**", "/categories/**",
                                        "/identities/**", "/images/**",
                                        "/products/**", "/security/**"
                                )
                                .uri("http://backend-server:9000")
                )
                .build();
    }
}
