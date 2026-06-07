package com.raghav.runboxspringboot.user.dto;

import com.raghav.runboxspringboot.user.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {

    private UUID userId;
    private String email;
    private Role role;
    private String accessToken;
    private String refreshToken;
}
