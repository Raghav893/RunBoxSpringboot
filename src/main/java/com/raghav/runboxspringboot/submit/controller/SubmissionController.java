package com.raghav.runboxspringboot.submit.controller;

import com.raghav.runboxspringboot.common.response.ApiResponse;
import com.raghav.runboxspringboot.submit.dto.SubmissionResponseDTO;
import com.raghav.runboxspringboot.submit.dto.SubmitRequestDTO;
import com.raghav.runboxspringboot.submit.service.SubmissionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SubmissionController {
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> submitCode(@Valid @RequestBody SubmitRequestDTO submitRequestDTO) {
        ApiResponse<SubmissionResponseDTO> apiResponse = ApiResponse.<SubmissionResponseDTO>builder()
                .data(submissionService.submit(submitRequestDTO))
                .success(true)
                .message("Response")
                .errors(null)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);


    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> getSubmissionById(@PathVariable UUID id) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Submission fetched successfully",
                submissionService.getSubmissionById(id),
                null
        ));
    }

    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponseDTO>>> getMySubmissions() {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "Submissions fetched successfully",
                submissionService.getMySubmission(),
                null
        ));
    }

}
