package com.hiba.logprocessingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LogProcessingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LogProcessingServiceApplication.class, args);
    }

}
