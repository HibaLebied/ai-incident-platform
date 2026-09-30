package com.hiba.logprocessingservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long originalLogId;

    @Column(nullable = false)
    private String serviceName;

    @Column(nullable = false)
    private String level;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    private LocalDateTime timestamp;

    private String environment;

    private String traceId;

    private String category;

    private Integer severityScore;

    private String httpMethod;

    private String endpoint;

    private Integer httpStatus;

    private Long responseTimeMs;

    private String ipAddress;

    private Integer port;

    private Boolean error;

    private Integer messageLength;

    private Boolean httpError;

    private Boolean slowRequest;

    private String fingerprint;

    private LocalDateTime processedAt;
}