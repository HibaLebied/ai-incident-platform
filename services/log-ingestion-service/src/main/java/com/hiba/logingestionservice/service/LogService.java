package com.hiba.logingestionservice.service;

import com.hiba.logingestionservice.dto.CreateLogRequest;
import com.hiba.logingestionservice.entity.Log;
import com.hiba.logingestionservice.event.LogCreatedEvent;
import com.hiba.logingestionservice.producer.LogEventProducer;
import com.hiba.logingestionservice.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final LogEventProducer logEventProducer;

    public Log createLog(CreateLogRequest request) {

        Log log = Log.builder()
                .serviceName(request.getServiceName())
                .level(request.getLevel())
                .message(request.getMessage())
                .timestamp(request.getTimestamp())
                .environment(request.getEnvironment())
                .traceId(request.getTraceId())
                .createdAt(LocalDateTime.now())
                .build();

        Log savedLog = logRepository.save(log);
        LogCreatedEvent event = new LogCreatedEvent(
                savedLog.getId(),
                savedLog.getServiceName(),
                savedLog.getLevel(),
                savedLog.getMessage(),
                savedLog.getTimestamp(),
                savedLog.getEnvironment(),
                savedLog.getTraceId()
        );
        logEventProducer.send(event);
        return savedLog;
    }
}
