package com.hiba.logprocessingservice.processor;

import com.hiba.logprocessingservice.model.ParsedLogData;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LogParser {

    private static final Pattern HTTP_PATTERN = Pattern.compile(
            "\\b(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS)\\s+(\\S+).*?\\b(\\d{3})\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern RESPONSE_TIME_PATTERN = Pattern.compile(
            "\\b(?:in|took|duration)\\s+(\\d+)\\s*ms\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern IP_PATTERN = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)\\.){3}" +
                    "(?:25[0-5]|2[0-4]\\d|1?\\d?\\d)\\b"
    );

    private static final Pattern PORT_PATTERN = Pattern.compile(
            "(?::|port\\s+)(\\d{2,5})\\b",
            Pattern.CASE_INSENSITIVE
    );

    public ParsedLogData parse(String message) {

        String httpMethod = null;
        String endpoint = null;
        Integer httpStatus = null;
        Long responseTimeMs = null;
        String ipAddress = null;
        Integer port = null;

        Matcher httpMatcher = HTTP_PATTERN.matcher(message);

        if (httpMatcher.find()) {
            httpMethod = httpMatcher.group(1).toUpperCase();
            endpoint = httpMatcher.group(2);

            try {
                httpStatus = Integer.parseInt(httpMatcher.group(3));
            } catch (NumberFormatException ignored) {
                // Keep null
            }
        }

        Matcher responseMatcher = RESPONSE_TIME_PATTERN.matcher(message);

        if (responseMatcher.find()) {
            try {
                responseTimeMs = Long.parseLong(responseMatcher.group(1));
            } catch (NumberFormatException ignored) {
                // Keep null
            }
        }

        Matcher ipMatcher = IP_PATTERN.matcher(message);

        if (ipMatcher.find()) {
            ipAddress = ipMatcher.group();
        }

        Matcher portMatcher = PORT_PATTERN.matcher(message);

        if (portMatcher.find()) {
            try {
                port = Integer.parseInt(portMatcher.group(1));
            } catch (NumberFormatException ignored) {
                // Keep null
            }
        }

        return new ParsedLogData(
                httpMethod,
                endpoint,
                httpStatus,
                responseTimeMs,
                ipAddress,
                port
        );
    }
}