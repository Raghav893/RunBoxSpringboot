package com.raghav.runboxspringboot.user.controller;

import com.raghav.runboxspringboot.common.response.ApiResponse;
import com.raghav.runboxspringboot.user.dto.AuthResponse;
import com.raghav.runboxspringboot.user.dto.LoginRequest;
import com.raghav.runboxspringboot.user.dto.RegisterRequest;
import com.raghav.runboxspringboot.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthResponse authResponse = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse<>(
                true,
                "User registered successfully",
                authResponse,
                null
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        AuthResponse authResponse = userService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Login successful",
                authResponse,
                null
        ));
    }
}
