package com.trung.gatewayservice.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {
    @Value("${jwt.secret}")
    private String secretKey;

    private final RouteValidator routeValidator;
    private final JwtUtil jwtUtil;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    public AuthenticationFilter(RouteValidator routeValidator, JwtUtil jwtUtil) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.jwtUtil = jwtUtil;
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
                try {
                    jwtUtil.validateToken(token);

                    Claims claims = jwtUtil.extractAllClaims(token);
                    String username = claims.getSubject();
                    String role = claims.get("role", String.class);

                    request = exchange.getRequest().mutate()
                            .header("X-Auth-User", username)
                            .header("X-Auth-Role", role)
                            .build();
                } catch (ExpiredJwtException e) {
                    return onError(exchange, "Token has expired", HttpStatus.UNAUTHORIZED);
                } catch (SignatureException e) {
                    return onError(exchange, "Invalid token signature", HttpStatus.UNAUTHORIZED);
                } catch (MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
                    return onError(exchange, "Malformed or invalid token", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange.mutate().request(request).build());
        };
    }

    public static class Config {
        // Các cấu hình tùy chỉnh cho filter nếu cần thiết
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
