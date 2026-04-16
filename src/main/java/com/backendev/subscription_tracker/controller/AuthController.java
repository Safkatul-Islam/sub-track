package com.backendev.subscription_tracker.controller;

import com.backendev.subscription_tracker.dto.RegistrationRequestDto;
import com.backendev.subscription_tracker.dto.UserResponseDto;
import com.backendev.subscription_tracker.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegistrationRequestDto request) {
        UserResponseDto created = authService.register(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
}