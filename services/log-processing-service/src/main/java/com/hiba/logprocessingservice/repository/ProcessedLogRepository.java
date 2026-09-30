package com.hiba.logprocessingservice.repository;

import com.hiba.logprocessingservice.entity.ProcessedLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProcessedLogRepository extends JpaRepository<ProcessedLog, Long> {

    boolean existsByOriginalLogId(Long originalLogId);

    Optional<ProcessedLog> findByOriginalLogId(Long originalLogId);
}