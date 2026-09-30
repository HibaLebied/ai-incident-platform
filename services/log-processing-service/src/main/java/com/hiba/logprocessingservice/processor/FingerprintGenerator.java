package com.hiba.logprocessingservice.processor;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class FingerprintGenerator {

    public String generate(String serviceName, String level, String message) {

        String template = normalizeVariableParts(message);

        String fingerprintSource =
                serviceName + "|" +
                        level + "|" +
                        template;

        return sha256(fingerprintSource);
    }

    private String normalizeVariableParts(String message) {

        return message
                // UUID
                .replaceAll(
                        "\\b[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-" +
                                "[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-" +
                                "[0-9a-fA-F]{12}\\b",
                        "<UUID>"
                )

                // IP addresses
                .replaceAll(
                        "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b",
                        "<IP>"
                )

                // Numbers
                .replaceAll(
                        "\\b\\d+\\b",
                        "<NUMBER>"
                )

                // Multiple spaces
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String sha256(String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash =
                    digest.digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {

            throw new IllegalStateException(
                    "SHA-256 algorithm not available",
                    e
            );
        }
    }
}