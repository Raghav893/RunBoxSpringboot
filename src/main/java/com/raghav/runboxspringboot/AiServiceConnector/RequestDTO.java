package com.raghav.runboxspringboot.AiServiceConnector;

import lombok.Data;

@Data
public class RequestDTO {
    private String sourceCode;
    private String language;
    private String stdin;
    private String stdout;
    private String stderr;

}
