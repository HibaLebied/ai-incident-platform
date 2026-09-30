package com.hiba.logprocessingservice.consumer;

import com.hiba.logprocessingservice.event.LogCreatedEvent;
import com.hiba.logprocessingservice.event.LogProcessedEvent;
import com.hiba.logprocessingservice.model.ProcessedLogData;
import com.hiba.logprocessingservice.processor.LogProcessor;
import com.hiba.logprocessingservice.producer.LogProcessedEventMapper;
import com.hiba.logprocessingservice.producer.LogProcessedProducer;
import com.hiba.logprocessingservice.service.ProcessedLogPersistenceService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class LogCreatedConsumer {

    private final LogProcessor processor;
    private final ProcessedLogPersistenceService persistenceService;

    public LogCreatedConsumer(
            LogProcessor processor,
            ProcessedLogPersistenceService persistenceService
    ) {
        this.processor = processor;
        this.persistenceService = persistenceService;
    }

    @KafkaListener(
            topics = "logs.raw",
            groupId = "log-processing-group"
    )
    public void consume(LogCreatedEvent event) {

        ProcessedLogData processed =
                processor.process(event);

        var result =
                persistenceService.saveIfNotProcessed(processed);

        if (!result.newlyCreated()) {

            System.out.println(
                    "Log already processed: "
                            + event.logId()
            );

            return;
        }

        System.out.println(
                "Log persisted and added to outbox: "
                        + event.logId()
        );
    }
}