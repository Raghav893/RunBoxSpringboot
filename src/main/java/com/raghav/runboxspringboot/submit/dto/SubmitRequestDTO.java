package com.raghav.runboxspringboot.submit.dto;

import com.raghav.runboxspringboot.submit.entity.Language;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NonNull;

@Data
public class SubmitRequestDTO {
    @NotBlank
    private String sourceCode;

    @NotBlank
    private String stdin;

    @NonNull
    private Language language;

}
