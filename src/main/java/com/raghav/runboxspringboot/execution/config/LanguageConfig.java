package com.raghav.runboxspringboot.execution.config;

public enum LanguageConfig {
    PYTHON ("python:3.11-slim","code.py",null ,new String[]{"python3","code.py"});
    public final String image;
    public final String filename;
    public final String[] compileCmd;  // null if interpreted
    public final String[] runCmd;


    LanguageConfig(String image, String filename, String[] compileCmd, String[] runCmd) {
        this.image = image;
        this.filename = filename;
        this.compileCmd = compileCmd;
        this.runCmd = runCmd;
    }
}
