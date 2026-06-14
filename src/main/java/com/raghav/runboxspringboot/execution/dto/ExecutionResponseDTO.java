package com.raghav.runboxspringboot.execution.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ExecutionResponseDTO {
    private UUID id;
    private UUID submissionId;
    private String stdout;
    private String stderr;
    private Integer exitCode;
    private Long executionTimeMs;
    private Long memoryUsedBytes;
    private LocalDateTime completedAt;
}
