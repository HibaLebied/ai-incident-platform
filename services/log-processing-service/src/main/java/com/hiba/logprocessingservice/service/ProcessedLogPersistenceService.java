package com.hiba.logprocessingservice.service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.hiba.logprocessingservice.entity.OutboxEvent;
import com.hiba.logprocessingservice.entity.ProcessedLog;
import com.hiba.logprocessingservice.model.ProcessedLogData;
import com.hiba.logprocessingservice.processor.ProcessedLogMapper;
import com.hiba.logprocessingservice.producer.LogProcessedEventMapper;
import com.hiba.logprocessingservice.event.LogProcessedEvent;
import com.hiba.logprocessingservice.repository.OutboxEventRepository;
import com.hiba.logprocessingservice.repository.ProcessedLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProcessedLogPersistenceService {

    private final ProcessedLogRepository repository;
    private final ProcessedLogMapper mapper;
    private final OutboxEventRepository outboxEventRepository;
    private final LogProcessedEventMapper eventMapper;
    private final ObjectMapper objectMapper;

    public ProcessedLogPersistenceService(
            ProcessedLogRepository repository,
            ProcessedLogMapper mapper,
            OutboxEventRepository outboxEventRepository,
            LogProcessedEventMapper eventMapper,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
        this.outboxEventRepository = outboxEventRepository;
        this.eventMapper = eventMapper;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public PersistenceResult saveIfNotProcessed(
            ProcessedLogData data
    ) {

        if (repository.existsByOriginalLogId(data.originalLogId())) {

            return new PersistenceResult(
                    repository.findByOriginalLogId(data.originalLogId())
                            .orElseThrow(),
                    false
            );
        }

        // 1. Save dans processed_logs
        ProcessedLog saved =
                repository.save(mapper.toEntity(data));

        // 2. Transformer ProcessedLogData → LogProcessedEvent
        LogProcessedEvent event =
                eventMapper.toEvent(data);

        try {

            // 3. Transformer l'event en JSON
            String payload =
                    objectMapper.writeValueAsString(event);

            // 4. Créer l'événement Outbox
            OutboxEvent outboxEvent =
                    OutboxEvent.builder()
                            .aggregateId(
                                    data.originalLogId().toString()
                            )
                            .eventType("LogProcessedEvent")
                            .topic("logs.processed")
                            .payload(payload)
                            .createdAt(LocalDateTime.now())
                            .build();

            // 5. Sauvegarder l'Outbox
            outboxEventRepository.save(outboxEvent);

        } catch (JacksonException e) {

            throw new RuntimeException(
                    "Failed to serialize LogProcessedEvent",
                    e
            );
        }

        return new PersistenceResult(
                saved,
                true
        );
    }

    public record PersistenceResult(
            ProcessedLog entity,
            boolean newlyCreated
    ) {
    }
}