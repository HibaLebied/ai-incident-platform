package com.hiba.incidentservice.repository;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

@Component
public class IncidentCorrelationLock {

    private final EntityManager entityManager;

    public IncidentCorrelationLock(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void lock(String serviceName, String environment, String fingerprint) {
        String correlationKey = serviceName + "|" + environment + "|" + fingerprint;

        entityManager.createNativeQuery(
                        "select pg_advisory_xact_lock(hashtextextended(:correlationKey, 0))"
                )
                .setParameter("correlationKey", correlationKey)
                .getSingleResult();
    }
}
