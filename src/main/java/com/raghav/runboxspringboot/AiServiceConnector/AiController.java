package com.raghav.runboxspringboot.AiServiceConnector;

import com.raghav.runboxspringboot.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
    private final AiService aiService;

    @PostMapping("/submissions/{submissionId}/check")
    public ResponseEntity<ApiResponse<ResponseDTO>> checkSubmission(
            @PathVariable UUID submissionId
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "AI review fetched successfully",
                aiService.checkSubmission(submissionId),
                null
        ));
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<ResponseDTO>> checkCode(
            @RequestBody RequestDTO requestDTO
    ) {
        return ResponseEntity.ok(new ApiResponse<>(
                true,
                "AI review fetched successfully",
                aiService.checkCode(requestDTO),
                null
        ));
    }
}
