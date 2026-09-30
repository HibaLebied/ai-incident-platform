package com.hiba.anomalydetectionservice.consumer;

import com.hiba.anomalydetectionservice.detection.AnomalyDetectionResult;
import com.hiba.anomalydetectionservice.detection.AnomalyDetector;
import com.hiba.anomalydetectionservice.event.AnomalyDetectedEvent;
import com.hiba.anomalydetectionservice.event.LogProcessedEvent;
import com.hiba.anomalydetectionservice.mapper.AnomalyEventMapper;
import com.hiba.anomalydetectionservice.producer.AnomalyDetectedProducer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Component
public class LogProcessedConsumer {

    private static final Logger log = LoggerFactory.getLogger(LogProcessedConsumer.class);

    private final AnomalyDetector anomalyDetector;
    private final AnomalyEventMapper eventMapper;
    private final AnomalyDetectedProducer producer;

    public LogProcessedConsumer(
            AnomalyDetector anomalyDetector,
            AnomalyEventMapper eventMapper,
            AnomalyDetectedProducer producer
    ) {
        this.anomalyDetector = anomalyDetector;
        this.eventMapper = eventMapper;
        this.producer = producer;
    }

    @KafkaListener(
            topics = "logs.processed",
            groupId = "anomaly-detection-group"
    )
    public void consume(LogProcessedEvent event) {

        log.info("Received processed log: {}", event.logId());

        List<AnomalyDetectionResult> results =
                anomalyDetector.detect(event);

        for (AnomalyDetectionResult result : results) {

            AnomalyDetectedEvent anomalyEvent =
                    eventMapper.toEvent(result);

            producer.publish(anomalyEvent);

            log.info("Anomaly detected: {}", result.anomalyType());
        }
    }
}
