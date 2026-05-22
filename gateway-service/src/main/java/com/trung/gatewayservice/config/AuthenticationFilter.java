package com.trung.gatewayservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final RouteValidator routeValidator;
    private final JwtUtil jwtUtil;
    private final ReactiveStringRedisTemplate redisTemplate;

    public AuthenticationFilter(RouteValidator routeValidator, JwtUtil jwtUtil, ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            if (routeValidator.isSecured.test(request)) {
                if (!request.getHeaders().containsHeader("Authorization")) {
                    return onError(exchange, "Authorization header is missing", HttpStatus.UNAUTHORIZED);
                }

                String authHeader = request.getHeaders().getFirst("Authorization");
                if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                    return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
                }

                String token = authHeader.substring(7);
                Claims claims;

                try {
                    claims = jwtUtil.extractAllClaims(token);
                } catch (ExpiredJwtException e) {
                    return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
                } catch (SignatureException e) {
                    return onError(exchange, "Invalid token signature", HttpStatus.UNAUTHORIZED);
                } catch (JwtException | IllegalArgumentException e) {
                    return onError(exchange, "Token error: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }

                String jti = claims.get("jti", String.class);
                String username = claims.getSubject();
                String role = claims.get("role", String.class);

                return redisTemplate.hasKey("blacklist:" + jti)
                        .flatMap(isBlacklisted -> {
                            if (Boolean.TRUE.equals(isBlacklisted)) {
                                return onError(exchange, "Token has been revoked", HttpStatus.UNAUTHORIZED);
                            }

                            // 4. Mutate the request AND pass the mutated version down the chain
                            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                                    .header("X-Auth-User", username)
                                    .header("X-Auth-Role", role)
                                    .build();

                            return chain.filter(exchange.mutate().request(mutatedRequest).build());
                        });
            }

            return chain.filter(exchange);
        };
    }

    public static class Config {
        // Custom configurations
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String jsonResponse = String.format("{\"timestamp\": %d, \"status\": %d, \"error\": \"%s\", \"message\": \"%s\"}",
                System.currentTimeMillis(), status.value(), status.getReasonPhrase(), message);

        DataBuffer buffer = response.bufferFactory().wrap(jsonResponse.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }
}