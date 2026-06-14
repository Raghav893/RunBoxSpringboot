package com.raghav.runboxspringboot.execution.controller;

import com.raghav.runboxspringboot.common.response.ApiResponse;
import com.raghav.runboxspringboot.execution.dto.ExecutionResponseDTO;
import com.raghav.runboxspringboot.execution.service.ExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/executions")
@RequiredArgsConstructor
public class ExecutionController {
    private final ExecutionService executionService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ExecutionResponseDTO>>> getMyExecutions() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Executions fetched successfully",
                executionService.getMyExecutions(),
                null
        ));
    }

    @GetMapping("/submissions/{submissionId}")
    public ResponseEntity<ApiResponse<ExecutionResponseDTO>> getExecutionBySubmissionId(
            @PathVariable UUID submissionId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Execution fetched successfully",
                executionService.getExecutionBySubmissionId(submissionId),
                null
        ));
    }
}
