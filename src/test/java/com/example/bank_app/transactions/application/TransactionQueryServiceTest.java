package com.example.bank_app.transactions.application;

import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.transactions.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionQueryServiceTest {

    @Test
    void getById_returnsOrThrows() {
        TransactionRepository repo = mock(TransactionRepository.class);
        TransactionQueryService svc = new TransactionQueryService(repo);
        UUID id = UUID.randomUUID();
        Transaction tx = new Transaction(UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("1"),
                com.example.bank_app.accounts.domain.Currency.RUB, LocalDateTime.now());
        when(repo.findById(id)).thenReturn(Optional.of(tx));
        assertSame(tx, svc.getById(id));

        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> svc.getById(id));
    }

    @Test
    void getByAccountId_delegates() {
        TransactionRepository repo = mock(TransactionRepository.class);
        TransactionQueryService svc = new TransactionQueryService(repo);
        UUID accId = UUID.randomUUID();
        when(repo.findByAccountId(accId)).thenReturn(List.of());
        assertEquals(List.of(), svc.getByAccountId(accId));
        verify(repo).findByAccountId(accId);
    }
}
