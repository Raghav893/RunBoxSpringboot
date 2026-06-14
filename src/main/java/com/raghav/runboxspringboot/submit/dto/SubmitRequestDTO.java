package com.raghav.runboxspringboot.submit.dto;

import com.raghav.runboxspringboot.submit.entity.Language;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NonNull;

@Data
public class SubmitRequestDTO {
    @NotBlank
    private String sourceCode;

    @NotNull
    private String stdin;

    @NonNull
    private Language language;

}
