package com.loanapplication.controllers;

import com.loanapplication.dto.*;
import com.loanapplication.security.JwtService;
import com.loanapplication.service.impl.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/totp/setup")
    public ResponseEntity<TotpSetupResponse> setupTotp(@RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("PRE_AUTH token required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        String stage = jwtService.extractStage(token);

        if (!"PRE_AUTH".equals(stage)) {throw new RuntimeException("PRE_AUTH token required");}

        return ResponseEntity.ok(authService.setupTotp(email));
    }

    @PostMapping("/totp/verify")
    public ResponseEntity<String> verifyTotp(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody TotpVerifyRequest request) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("PRE_AUTH token required");
        }

        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token);
        String stage = jwtService.extractStage(token);
        if (!"PRE_AUTH".equals(stage)) {
            throw new RuntimeException("PRE_AUTH token required");
        }

        return ResponseEntity.ok(authService.verifyTotp(email, request.getCode()));
    }


    @PostMapping("/email-verification/send")
    public ResponseEntity<String> sendEmailVerificationOtp(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                authService.sendEmailOtp(email)
        );
    }

    @PostMapping("/email-verification/verify")
    public ResponseEntity<String> verifyEmailVerificationOtp(
            Authentication authentication,
            @Valid @RequestBody EmailOtpVerifyRequest request) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                authService.verifyEmailOtp(email, request.getOtp())
        );
    }

}