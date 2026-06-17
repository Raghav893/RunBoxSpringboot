package com.raghav.runboxspringboot.worker;

import com.raghav.runboxspringboot.execution.entity.Execution;
import com.raghav.runboxspringboot.execution.repo.ExecutionRepository;
import com.raghav.runboxspringboot.execution.service.DockerCodeExecutionService;
import com.raghav.runboxspringboot.security.SecurityUtils;
import com.raghav.runboxspringboot.submit.entity.Status;
import com.raghav.runboxspringboot.submit.entity.Submission;
import com.raghav.runboxspringboot.submit.repo.SubmissionRepository;
import com.raghav.runboxspringboot.worker.service.RedisQueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExecutionWorker {
    private final RedisQueueService redisQueueService;
    private final DockerCodeExecutionService dockerCodeExecutionService;
    private final SubmissionRepository submissionRepository;
    private final ExecutionRepository executionRepository;


    @Scheduled(fixedDelay = 500)
    public void  pollQueue(){
        UUID submissionId = redisQueueService.dequeue();
        if (submissionId == null) {
            return;
        }
        Submission submission = submissionRepository.getSubmissionsBySubmissionId(submissionId)
                .orElseThrow(()->new RuntimeException("Submission not found "+ submissionId));

        try {
            submission.setStatus(Status.RUNNING);
            submissionRepository.save(submission);
            Execution result = dockerCodeExecutionService.executionResultGenerator(submission);
            executionRepository.save(result);
            submission.setStatus(Status.COMPLETED);
            submissionRepository.save(submission);
            log.info("Job completed: {}", submissionId);

        }
        catch (TimeoutException e) {
            submission.setStatus(Status.TIMEOUT);
            submissionRepository.save(submission);
            log.warn("Job timed out: {}", submissionId);
        }
        catch (Exception e) {
            submission.setStatus(Status.ERROR);
            submissionRepository.save(submission);
            log.error("Job failed: {} — {}", submissionId, e.getMessage());
        }
    }

}
