package com.example.bank_app.audit.application;

import com.example.bank_app.audit.domain.AuditLog;
import com.example.bank_app.audit.infrastructure.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void log(String action, String details) {
        repository.save(new AuditLog(action, details));
    }
}
