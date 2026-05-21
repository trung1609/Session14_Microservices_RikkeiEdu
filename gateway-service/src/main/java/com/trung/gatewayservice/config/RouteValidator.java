package com.trung.gatewayservice.config;


import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.function.Predicate;

public class RouteValidator {
    public static final List<String> openApiEndpoints =
            List.of("/identity/api/auth/**");

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
