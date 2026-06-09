package com.raghav.runboxspringboot.submit.controller;

import com.raghav.runboxspringboot.common.response.ApiResponse;
import com.raghav.runboxspringboot.submit.dto.SubmissionResponseDTO;
import com.raghav.runboxspringboot.submit.dto.SubmitRequestDTO;
import com.raghav.runboxspringboot.submit.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api")
public class SubmissionController {
    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    @PostMapping("/submit")
    public ResponseEntity<ApiResponse<SubmissionResponseDTO>> submitCode(@RequestBody SubmitRequestDTO submitRequestDTO) {
        ApiResponse<SubmissionResponseDTO> apiResponse = ApiResponse.<SubmissionResponseDTO>builder()
                .data(submissionService.submit(submitRequestDTO))
                .success(true)
                .message("Response")
                .errors(null)
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);


    }

}
