package com.trung.productservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String username = request.getHeader("X-Auth-User");
        String rolesHeader = request.getHeader("X-Auth-Role");

        List<GrantedAuthority> authorities = Arrays.stream(rolesHeader != null ? rolesHeader.split(",") : new String[0])
                .map(String::trim)
                .filter(role -> !role.isEmpty())
                .map(role -> {
                    String finalRole = role.toUpperCase().startsWith("ROLE_") ? role.toUpperCase() : "ROLE_" + role.toUpperCase();
                    return (GrantedAuthority) () -> finalRole;
                })
                .toList();

        if (username != null && rolesHeader != null) {
            System.out.println("==> Product Service nhận được Request từ Gateway!");
            System.out.println("User: " + username + " | Raw Roles Header: " + rolesHeader);
            System.out.println("Mapped Authorities: " + authorities);

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                    username, null, authorities
            );

            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        filterChain.doFilter(request, response);
    }
}
