package com.hiba.logingestionservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateLogRequest {

    @NotBlank
    private String serviceName;

    @NotBlank
    private String level;

    @NotBlank
    private String message;

    @NotNull
    private LocalDateTime timestamp;

    private String environment;

    private String traceId;
}
