package com.raghav.runboxspringboot.worker;

import com.raghav.runboxspringboot.execution.entity.Execution;
import com.raghav.runboxspringboot.execution.repo.ExecutionRepository;
import com.raghav.runboxspringboot.execution.service.DockerCodeExecutionService;

import com.raghav.runboxspringboot.submit.entity.Status;
import com.raghav.runboxspringboot.submit.entity.Submission;
import com.raghav.runboxspringboot.submit.repo.SubmissionRepository;
import com.raghav.runboxspringboot.worker.service.RedisQueueService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeoutException;


@Component

@Slf4j
public class ExecutionWorker {
    private final RedisQueueService redisQueueService;
    private final DockerCodeExecutionService dockerCodeExecutionService;
    private final SubmissionRepository submissionRepository;
    private final ExecutionRepository executionRepository;
    @Qualifier("executionTaskExecutor")
    private final TaskExecutor taskExecutor;

    public ExecutionWorker(RedisQueueService redisQueueService, DockerCodeExecutionService dockerCodeExecutionService, SubmissionRepository submissionRepository, ExecutionRepository executionRepository, @Qualifier("executionTaskExecutor") TaskExecutor taskExecutor) {
        this.redisQueueService = redisQueueService;
        this.dockerCodeExecutionService = dockerCodeExecutionService;
        this.submissionRepository = submissionRepository;
        this.executionRepository = executionRepository;
        this.taskExecutor = taskExecutor;
    }


    @Scheduled(fixedDelay = 500)
    public void pollQueue() {
        UUID submissionId = redisQueueService.dequeue();

        if (submissionId == null) return;

        taskExecutor.execute(() -> processJob(submissionId));  // ← just dispatch
    }

    private void processJob(UUID submissionId) {
        Submission submission = submissionRepository.getSubmissionsBySubmissionId(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found " + submissionId));

        try {
            submission.setStatus(Status.RUNNING);
            submissionRepository.save(submission);

            Execution result = dockerCodeExecutionService.executionResultGenerator(submission);
            executionRepository.save(result);

            if (result.getExitCode() == -1) {
                submission.setStatus(Status.TIMEOUT);
            }
            else {
                submission.setStatus(Status.COMPLETED);
            }
            submissionRepository.save(submission);
            log.info("Job completed: {} status: {}", submissionId, submission.getStatus());


        }
        catch (TimeoutException e) {                        // ← specific, catches first
            submission.setStatus(Status.TIMEOUT);
            submissionRepository.save(submission);
            log.warn("Job timed out: {}", submissionId);
        }
        catch (Exception e) {
            submission.setStatus(Status.ERROR);
            submissionRepository.save(submission);
            log.error("Job failed: {}", e.getMessage());
        }
    }}

/*
Things left to do
    more language support
    AI side service to resolve errors
    CORS
    A frontend if needed
    DockerCompose
    Ci Cd
 */