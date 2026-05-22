package com.trung.productservice.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        List<String> permisstions = source.getClaimAsStringList("permissions");
        if (permisstions == null || permisstions.isEmpty()){
            return new JwtAuthenticationToken(source, Collections.emptyList());
        }

        List<SimpleGrantedAuthority> authorities = permisstions.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new JwtAuthenticationToken(source, authorities);
    }
}
