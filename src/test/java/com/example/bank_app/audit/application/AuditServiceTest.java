package com.example.bank_app.audit.application;

import com.example.bank_app.audit.domain.AuditLog;
import com.example.bank_app.audit.infrastructure.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditServiceTest {

    @Test
    void log_savesAuditLogEntity() {
        AuditLogRepository repo = mock(AuditLogRepository.class);
        AuditService svc = new AuditService(repo);

        svc.log("A", "D");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repo).save(captor.capture());
        assertEquals("A", captor.getValue().getAction());
        assertEquals("D", captor.getValue().getDetails());
        assertNotNull(captor.getValue().getCreatedAt());
    }
}
