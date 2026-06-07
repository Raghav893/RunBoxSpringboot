package com.raghav.runboxspringboot.common.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<String> errors;

}
