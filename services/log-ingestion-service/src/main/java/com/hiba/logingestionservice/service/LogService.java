package com.hiba.logingestionservice.service;

import com.hiba.logingestionservice.dto.CreateLogRequest;
import com.hiba.logingestionservice.entity.OutboxEvent;
import com.hiba.logingestionservice.entity.Log;
import com.hiba.logingestionservice.event.LogCreatedEvent;
import com.hiba.logingestionservice.repository.OutboxEventRepository;
import com.hiba.logingestionservice.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
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
        try {
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(savedLog.getId().toString())
                    .eventType("LogCreatedEvent")
                    .topic("logs.raw")
                    .payload(objectMapper.writeValueAsString(event))
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxEventRepository.save(outboxEvent);
        } catch (JacksonException exception) {
            throw new IllegalStateException("Failed to serialize LogCreatedEvent", exception);
        }

        return savedLog;
    }
}
