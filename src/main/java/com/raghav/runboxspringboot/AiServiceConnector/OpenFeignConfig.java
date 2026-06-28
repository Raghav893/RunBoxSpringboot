package com.raghav.runboxspringboot.AiServiceConnector;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ai-service", url = "${ai.service.url:http://localhost:8000}")
public interface OpenFeignConfig {

    @PostMapping("/aicheck")
    ResponseDTO checkCode(@RequestBody RequestDTO requestDTO);
}

