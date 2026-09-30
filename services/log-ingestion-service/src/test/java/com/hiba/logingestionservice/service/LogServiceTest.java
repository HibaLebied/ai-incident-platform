package com.hiba.logingestionservice.service;

import com.hiba.logingestionservice.dto.CreateLogRequest;
import com.hiba.logingestionservice.entity.Log;
import com.hiba.logingestionservice.entity.OutboxEvent;
import com.hiba.logingestionservice.repository.LogRepository;
import com.hiba.logingestionservice.repository.OutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    private LogService logService;

    @BeforeEach
    void setUp() {
        logService = new LogService(logRepository, outboxEventRepository, objectMapper);
    }

    @Test
    void savesLogAndOutboxEventTogether() throws Exception {
        when(logRepository.save(any(Log.class))).thenAnswer(invocation -> {
            Log log = invocation.getArgument(0);
            log.setId(42L);
            return log;
        });
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"logId\":42}");

        Log savedLog = logService.createLog(request());

        assertEquals(42L, savedLog.getId());

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertEquals("42", captor.getValue().getAggregateId());
        assertEquals("LogCreatedEvent", captor.getValue().getEventType());
        assertEquals("logs.raw", captor.getValue().getTopic());
        assertEquals("{\"logId\":42}", captor.getValue().getPayload());
    }

    private CreateLogRequest request() {
        CreateLogRequest request = new CreateLogRequest();
        request.setServiceName("payment-service");
        request.setLevel("ERROR");
        request.setMessage("Database connection failed");
        request.setTimestamp(LocalDateTime.of(2026, 9, 24, 18, 0));
        request.setEnvironment("production");
        request.setTraceId("trace-42");
        return request;
    }
}
