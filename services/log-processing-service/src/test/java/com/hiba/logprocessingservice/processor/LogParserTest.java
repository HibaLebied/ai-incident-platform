package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.model.ParsedLogData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogParserTest {

    private final LogParser parser = new LogParser();

    @Test
    void shouldParseHttpLog() {

        String message =
                "GET /api/users returned 500 in 235ms";

        ParsedLogData result =
                parser.parse(message);

        assertEquals("GET", result.httpMethod());
        assertEquals("/api/users", result.endpoint());
        assertEquals(500, result.httpStatus());
        assertEquals(235L, result.responseTimeMs());
    }

    @Test
    void shouldParsePostRequest() {

        String message =
                "POST /api/payments returned 201 in 120ms";

        ParsedLogData result =
                parser.parse(message);

        assertEquals("POST", result.httpMethod());
        assertEquals("/api/payments", result.endpoint());
        assertEquals(201, result.httpStatus());
        assertEquals(120L, result.responseTimeMs());
    }

    @Test
    void shouldParseIpAddress() {

        String message =
                "Request from 192.168.1.10";

        ParsedLogData result =
                parser.parse(message);

        assertEquals(
                "192.168.1.10",
                result.ipAddress()
        );
    }

    @Test
    void shouldParsePort() {

        String message =
                "Connection failed on port 8080";

        ParsedLogData result =
                parser.parse(message);

        assertEquals(8080, result.port());
    }

    @Test
    void shouldReturnNullForMissingHttpInformation() {

        String message =
                "Application started successfully";

        ParsedLogData result =
                parser.parse(message);

        assertNull(result.httpMethod());
        assertNull(result.endpoint());
        assertNull(result.httpStatus());
        assertNull(result.responseTimeMs());
    }
}