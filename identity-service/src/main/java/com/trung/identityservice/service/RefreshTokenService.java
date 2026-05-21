package com.trung.identityservice.service;

import com.trung.identityservice.config.JwtUtil;
import com.trung.identityservice.dto.JwtResponse;
import com.trung.identityservice.dto.RefreshTokenRequest;
import com.trung.identityservice.entity.RefreshToken;
import com.trung.identityservice.entity.Users;
import com.trung.identityservice.repository.AuthRepository;
import com.trung.identityservice.repository.RefreshTokenRepository;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthRepository authRepository;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenDurationMs;


    public RefreshToken createRefreshToken(String userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(authRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());
        refreshToken.setIsRevoked(false);

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new signin request.");
        }
        return token;
    }
}
