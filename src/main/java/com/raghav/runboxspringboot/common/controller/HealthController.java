package com.raghav.runboxspringboot.common.controller;


import com.raghav.runboxspringboot.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public ResponseEntity<ApiResponse<Map<String, String>>> health() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "service is healthy",
                Map.of("status", "UP"),
                null
        ));
    }
}
