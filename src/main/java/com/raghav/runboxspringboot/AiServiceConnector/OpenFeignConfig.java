package com.raghav.runboxspringboot.AiServiceConnector;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(url = "https://localhost:8000",name = "AiService")
public interface OpenFeignConfig {

    @GetMapping("/aicheck")
    ResponseDTO AiChecker(@RequestBody RequestDTO requestDTO);
}

