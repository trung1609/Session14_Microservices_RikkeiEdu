package com.trung.identityservice.config;

import com.trung.identityservice.entity.Users;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationTime;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationTime;

    public SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public String generateAccessToken(Users users) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", users.getRole());
        claims.put("type", "access");

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(users.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + accessTokenExpirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Users users) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", users.getRole());
        claims.put("type", "refresh");

        Date now = new Date();

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(users.getUsername())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshTokenExpirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }
}
