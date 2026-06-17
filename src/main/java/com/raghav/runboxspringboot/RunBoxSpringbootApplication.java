package com.raghav.runboxspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RunBoxSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(RunBoxSpringbootApplication.class, args);
    }

}
