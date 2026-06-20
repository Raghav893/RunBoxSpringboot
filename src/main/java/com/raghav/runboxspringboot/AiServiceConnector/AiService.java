package com.raghav.runboxspringboot.AiServiceConnector;

import org.springframework.stereotype.Service;

@Service
public class AiService {
    private final OpenFeignConfig feignConfig;

    public AiService(OpenFeignConfig feignConfig) {
        this.feignConfig = feignConfig;
    }
    private RequestDTO response


}
