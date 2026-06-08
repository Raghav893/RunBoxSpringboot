package com.raghav.runboxspringboot.submit.dto;

import com.raghav.runboxspringboot.submit.entity.Language;
import com.raghav.runboxspringboot.submit.entity.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class SubmissionResponseDTO {
    private UUID submissionId;
    private String sourceCode;
    private String stdin;
    private Status status;
    private Language language;
    private LocalDateTime submittedAt;
}
