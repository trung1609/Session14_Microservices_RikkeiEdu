package com.trung.identityservice.controller;

import com.trung.identityservice.dto.FormLogin;
import com.trung.identityservice.dto.FormRegister;
import com.trung.identityservice.dto.JwtResponse;
import com.trung.identityservice.dto.RefreshTokenRequest;
import com.trung.identityservice.service.AuthService;
import com.trung.identityservice.service.RefreshTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody FormRegister request){
        String response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody FormLogin request){
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(refreshTokenService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request,  @RequestParam String refreshToken){
        authService.logout(request,refreshToken);
        return ResponseEntity.ok("Logged out successfully");
    }
}

