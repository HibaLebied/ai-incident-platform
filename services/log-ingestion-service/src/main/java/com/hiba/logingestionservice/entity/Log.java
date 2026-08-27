package com.hiba.logingestionservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Log {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String serviceName;

    private String level;

    @Column(columnDefinition = "TEXT")
    private String message;

    private LocalDateTime timestamp;

    private String environment;

    private String traceId;

    private LocalDateTime createdAt;
}
