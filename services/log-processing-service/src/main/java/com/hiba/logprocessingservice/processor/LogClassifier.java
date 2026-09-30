package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.model.LogCategory;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LogClassifier {

    public LogCategory classify(String message) {

        String normalizedMessage = message.toLowerCase(Locale.ROOT);

        if (containsAny(normalizedMessage,
                "database",
                "sql",
                "postgres",
                "mysql",
                "jdbc",
                "connection refused")) {

            return LogCategory.DATABASE;
        }

        if (containsAny(normalizedMessage,
                "http",
                "request",
                "response",
                "status code",
                "endpoint",
                "404",
                "500",
                "502",
                "503")) {

            return LogCategory.HTTP;
        }

        if (containsAny(normalizedMessage,
                "jwt",
                "authentication",
                "login",
                "logout",
                "invalid token",
                "unauthorized")) {

            return LogCategory.AUTHENTICATION;
        }

        if (containsAny(normalizedMessage,
                "timeout",
                "connection",
                "socket",
                "network",
                "dns")) {

            return LogCategory.NETWORK;
        }

        if (containsAny(normalizedMessage,
                "permission denied",
                "forbidden",
                "attack",
                "security",
                "access denied")) {

            return LogCategory.SECURITY;
        }

        if (containsAny(normalizedMessage,
                "exception",
                "nullpointerexception",
                "illegalargumentexception",
                "runtime exception")) {

            return LogCategory.APPLICATION;
        }

        if (containsAny(normalizedMessage,
                "cpu",
                "memory",
                "disk",
                "shutdown",
                "restart")) {

            return LogCategory.SYSTEM;
        }

        return LogCategory.UNKNOWN;
    }

    private boolean containsAny(String value, String... keywords) {

        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }

        return false;
    }
}