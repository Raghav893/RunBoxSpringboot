package com.raghav.runboxspringboot.submit.service;

import com.raghav.runboxspringboot.common.exception.SubmissionNotFoundException;
import com.raghav.runboxspringboot.security.SecurityUtils;
import com.raghav.runboxspringboot.submit.dto.SubmissionResponseDTO;
import com.raghav.runboxspringboot.submit.dto.SubmitRequestDTO;
import com.raghav.runboxspringboot.submit.entity.Status;
import com.raghav.runboxspringboot.submit.entity.Submission;
import com.raghav.runboxspringboot.submit.repo.SubmissionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SubmissionService {
    private final SubmissionRepository submissionRepository;


    public SubmissionService(SubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }
    public SubmissionResponseDTO submit(SubmitRequestDTO submitRequestDTO){
        Submission submission = Submission.builder()
                .user(SecurityUtils.getCurrentUser())
                .submittedAt(LocalDateTime.now())
                .language(submitRequestDTO.getLanguage())
                .stdin(submitRequestDTO.getStdin())
                .status(Status.PENDING)
                .build();

        submissionRepository.save(submission);
        return toResponse(submission);

    }
    public SubmissionResponseDTO getSubmissionById(UUID id){
        return toResponse( submissionRepository.getSubmissionsBySubmissionIdAndUser(id,SecurityUtils.getCurrentUser())
                .orElseThrow(()->new SubmissionNotFoundException("submission not found")));
    }


    private SubmissionResponseDTO toResponse(Submission submission){

        return SubmissionResponseDTO.builder()
                .submissionId(submission.getSubmissionId())
                .sourceCode(submission.getSourceCode())
                .status(submission.getStatus())
                .stdin(submission.getStdin())
                .language(submission.getLanguage())
                .submittedAt(submission.getSubmittedAt()).build();
    }
}
