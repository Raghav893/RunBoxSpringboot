package com.raghav.runboxspringboot.AiServiceConnector;

import lombok.Data;

@Data
public class ResponseDTO {
    private String sourceCode;
    private String language;
    private String stdin;
    private String stdout;
    private String stderr;
    private String correctedCode;
    private boolean hasErrors;
    private String aiRemarks;
    private String improvements;
}
