package com.raghav.runboxspringboot.execution.service;

import com.raghav.runboxspringboot.common.exception.SubmissionNotFoundException;
import com.raghav.runboxspringboot.execution.dto.ExecutionResponseDTO;
import com.raghav.runboxspringboot.execution.entity.Execution;
import com.raghav.runboxspringboot.execution.repo.ExecutionRepository;
import com.raghav.runboxspringboot.security.SecurityUtils;
import com.raghav.runboxspringboot.submit.entity.Status;
import com.raghav.runboxspringboot.submit.entity.Submission;
import com.raghav.runboxspringboot.submit.repo.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExecutionService {
    private static final int TIMEOUT_EXIT_CODE = -1;

    private final DockerCodeExecutionService dockerCodeExecutionService;
    private final ExecutionRepository executionRepository;
    private final SubmissionRepository submissionRepository;

    @Transactional
    public ExecutionResponseDTO runSubmission(Submission submission) {
        submission.setStatus(Status.RUNNING);
        submission = submissionRepository.save(submission);

        Execution execution;
        try {
            execution = dockerCodeExecutionService.executionResultGenerator(submission);
            submission.setStatus(resolveStatus(execution));
        } catch (Exception exception) {
            execution = buildFailedExecution(submission, exception);
            submission.setStatus(Status.ERROR);
        }

        Execution savedExecution = executionRepository.save(execution);
        submission.setExecution(savedExecution);
        submissionRepository.save(submission);
        return toResponse(savedExecution);
    }

    @Transactional(readOnly = true)
    public ExecutionResponseDTO getExecutionBySubmissionId(UUID submissionId) {
        Execution execution = executionRepository.findBySubmissionSubmissionIdAndSubmissionUser(
                        submissionId,
                        SecurityUtils.getCurrentUser()
                )
                .orElseThrow(() -> new SubmissionNotFoundException("execution not found"));

        return toResponse(execution);
    }

    @Transactional(readOnly = true)
    public List<ExecutionResponseDTO> getMyExecutions() {
        return executionRepository.findBySubmissionUser(SecurityUtils.getCurrentUser())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private Status resolveStatus(Execution execution) {
        if (execution.getExitCode() == TIMEOUT_EXIT_CODE) {
            return Status.TIMEOUT;
        }
        if (execution.getExitCode() == 0) {
            return Status.COMPLETED;
        }
        return Status.ERROR;
    }

    private Execution buildFailedExecution(Submission submission, Exception exception) {
        return Execution.builder()
                .submission(submission)
                .stdout("")
                .stderr(exception.getMessage())
                .exitCode(1)
                .executionTimeMs(0L)
                .completedAt(LocalDateTime.now())
                .build();
    }

    private ExecutionResponseDTO toResponse(Execution execution) {
        return ExecutionResponseDTO.builder()
                .id(execution.getId())
                .submissionId(execution.getSubmission().getSubmissionId())
                .stdout(execution.getStdout())
                .stderr(execution.getStderr())
                .exitCode(execution.getExitCode())
                .executionTimeMs(execution.getExecutionTimeMs())
                .memoryUsedBytes(execution.getMemoryUsedBytes())
                .completedAt(execution.getCompletedAt())
                .build();
    }
}
