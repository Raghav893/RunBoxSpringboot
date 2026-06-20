package com.raghav.runboxspringboot.AiServiceConnector;

import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(url = "https://localhost:8000",name = "AiService")
public interface OpenFeignConfig {
}
