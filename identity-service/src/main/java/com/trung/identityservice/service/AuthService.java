package com.trung.identityservice.service;

import com.trung.identityservice.config.JwtUtil;
import com.trung.identityservice.dto.FormLogin;
import com.trung.identityservice.dto.FormRegister;
import com.trung.identityservice.dto.JwtResponse;
import com.trung.identityservice.entity.RefreshToken;
import com.trung.identityservice.entity.Users;
import com.trung.identityservice.repository.AuthRepository;
import com.trung.identityservice.repository.RefreshTokenRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthRepository authRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpirationTime;
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpirationTime;

    public String register(FormRegister request) {
        Users users = new Users();
        users.setUsername(request.getUsername());
        users.setPassword(passwordEncoder.encode(request.getPassword()));
        users.setRole(request.getRole());
        users.setPermissions(request.getPermissions());
        authRepository.save(users);
        return "User registered successfully";
    }

    public JwtResponse login(FormLogin request) {
        Users users = authRepository.findByUsername(request.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), users.getPassword())) {
            throw new RuntimeException("Invalid username or password");
        }
        String accessToken = jwtUtil.generateAccessToken(users);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(users.getId());

        return new JwtResponse(accessToken, refreshToken.getToken(), accessTokenExpirationTime, refreshTokenExpirationTime);
    }

    public void logout(HttpServletRequest request, String refreshToken){

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new JwtException("Missing or invalid Authorization header");
        }
        String accessToken = authHeader.substring(7);
        try {
            String jti = jwtUtil.extractJti(accessToken);
            long expiration = jwtUtil.extractExpiration(accessToken);
            long ttl = expiration - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set("blacklist:"+jti, "logout", ttl).block();
            }
        }catch (ExpiredJwtException e){
            throw new JwtException("Token has expired");
        } catch (Exception e) {
            throw new JwtException("Invalid token");
        }

        RefreshToken refreshTokenEntity = refreshTokenRepository.findByToken(refreshToken).orElseThrow(() -> new RuntimeException("Refresh token not found"));
        refreshTokenRepository.delete(refreshTokenEntity);
    }
}
