package com.hiba.logingestionservice.controller;

import com.hiba.logingestionservice.dto.CreateLogRequest;
import com.hiba.logingestionservice.entity.Log;
import com.hiba.logingestionservice.service.LogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @PostMapping
    public ResponseEntity<Log> createLog(
            @Valid @RequestBody CreateLogRequest request) {

        Log createdLog = logService.createLog(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdLog);
    }
}
