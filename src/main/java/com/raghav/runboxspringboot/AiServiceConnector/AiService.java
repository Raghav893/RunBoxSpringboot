package com.raghav.runboxspringboot.AiServiceConnector;

import com.raghav.runboxspringboot.common.exception.SubmissionNotFoundException;
import com.raghav.runboxspringboot.execution.entity.Execution;
import com.raghav.runboxspringboot.execution.repo.ExecutionRepository;
import com.raghav.runboxspringboot.security.SecurityUtils;
import com.raghav.runboxspringboot.submit.entity.Submission;
import com.raghav.runboxspringboot.submit.repo.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiService {
    private final OpenFeignConfig aiClient;
    private final SubmissionRepository submissionRepository;
    private final ExecutionRepository executionRepository;

    @Transactional(readOnly = true)
    public ResponseDTO checkSubmission(UUID submissionId) {
        Submission submission = submissionRepository
                .getSubmissionsBySubmissionIdAndUser(submissionId, SecurityUtils.getCurrentUser())
                .orElseThrow(() -> new SubmissionNotFoundException("submission not found"));

        Execution execution = executionRepository
                .findBySubmissionSubmissionIdAndSubmissionUser(submissionId, SecurityUtils.getCurrentUser())
                .orElse(null);

        return aiClient.checkCode(toRequest(submission, execution));
    }

    public ResponseDTO checkCode(RequestDTO requestDTO) {
        return aiClient.checkCode(requestDTO);
    }

    private RequestDTO toRequest(Submission submission, Execution execution) {
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setSourceCode(submission.getSourceCode());
        requestDTO.setLanguage(submission.getLanguage().name());
        requestDTO.setStdin(nullToEmpty(submission.getStdin()));
        requestDTO.setStdout(execution == null ? "" : nullToEmpty(execution.getStdout()));
        requestDTO.setStderr(execution == null ? "" : nullToEmpty(execution.getStderr()));
        return requestDTO;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
