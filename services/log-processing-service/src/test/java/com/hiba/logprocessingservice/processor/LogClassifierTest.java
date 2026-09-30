package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.model.LogCategory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LogClassifierTest {

    private final LogClassifier classifier = new LogClassifier();

    @Test
    void shouldClassifyDatabaseLog() {

        LogCategory result =
                classifier.classify(
                        "Database connection failed"
                );

        assertEquals(LogCategory.DATABASE, result);
    }

    @Test
    void shouldClassifyHttpLog() {

        LogCategory result =
                classifier.classify(
                        "GET /api/users returned 500"
                );

        assertEquals(LogCategory.HTTP, result);
    }

    @Test
    void shouldClassifyAuthenticationLog() {

        LogCategory result =
                classifier.classify(
                        "Authentication failed for user"
                );

        assertEquals(
                LogCategory.AUTHENTICATION,
                result
        );
    }

    @Test
    void shouldClassifyNetworkLog() {

        LogCategory result =
                classifier.classify(
                        "Connection timeout to server"
                );

        assertEquals(
                LogCategory.NETWORK,
                result
        );
    }

    @Test
    void shouldReturnUnknownForUnrecognizedMessage() {

        LogCategory result =
                classifier.classify(
                        "Something completely unknown happened"
                );

        assertEquals(
                LogCategory.UNKNOWN,
                result
        );
    }
}