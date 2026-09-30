package com.hiba.logprocessingservice.processor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FingerprintGeneratorTest {

    private final FingerprintGenerator generator =
            new FingerprintGenerator();

    @Test
    void sameDynamicValuesShouldProduceSameFingerprint() {

        String message1 =
                "User 123 failed from 192.168.1.10";

        String message2 =
                "User 456 failed from 10.0.0.5";

        String fingerprint1 =
                generator.generate(
                        "payment-service",
                        "ERROR",
                        message1
                );

        String fingerprint2 =
                generator.generate(
                        "payment-service",
                        "ERROR",
                        message2
                );

        assertEquals(
                fingerprint1,
                fingerprint2
        );
    }

    @Test
    void differentMessagesShouldProduceDifferentFingerprint() {

        String fingerprint1 =
                generator.generate(
                        "payment-service",
                        "ERROR",
                        "Database connection failed"
                );

        String fingerprint2 =
                generator.generate(
                        "payment-service",
                        "ERROR",
                        "Authentication failed"
                );

        assertNotEquals(
                fingerprint1,
                fingerprint2
        );
    }

    @Test
    void differentServicesShouldProduceDifferentFingerprint() {

        String fingerprint1 =
                generator.generate(
                        "payment-service",
                        "ERROR",
                        "Connection failed"
                );

        String fingerprint2 =
                generator.generate(
                        "user-service",
                        "ERROR",
                        "Connection failed"
                );

        assertNotEquals(
                fingerprint1,
                fingerprint2
        );
    }

    @Test
    void sameInputShouldProduceSameFingerprint() {

        String message =
                "GET /api/users returned 500";

        String fingerprint1 =
                generator.generate(
                        "payment-service",
                        "ERROR",
                        message
                );

        String fingerprint2 =
                generator.generate(
                        "payment-service",
                        "ERROR",
                        message
                );

        assertEquals(
                fingerprint1,
                fingerprint2
        );
    }
}