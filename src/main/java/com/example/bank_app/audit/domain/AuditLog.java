package com.example.bank_app.audit.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue
    private Long id;

    private String action;
    private String details;
    private LocalDateTime createdAt;

    protected AuditLog() {}

    public AuditLog(String action, String details) {
        this.action = action;
        this.details = details;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
