package com.example.bank_app.transactions.infrastructure;

import com.example.bank_app.transactions.domain.Transaction;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaTransactionRepositoryAdapterTest {

    @Test
    void adapter_delegatesToSpringDataRepository() {
        TransactionJpaRepository jpa = mock(TransactionJpaRepository.class);
        JpaTransactionRepositoryAdapter adapter = new JpaTransactionRepositoryAdapter(jpa);

        UUID id = UUID.randomUUID();
        UUID accId = UUID.randomUUID();
        Transaction tx = mock(Transaction.class);

        when(jpa.save(tx)).thenReturn(tx);
        when(jpa.findById(id)).thenReturn(Optional.of(tx));
        when(jpa.findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accId, accId)).thenReturn(List.of(tx));

        assertSame(tx, adapter.save(tx));
        assertEquals(Optional.of(tx), adapter.findById(id));
        assertEquals(List.of(tx), adapter.findByAccountId(accId));

        verify(jpa).save(tx);
        verify(jpa).findById(id);
        verify(jpa).findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accId, accId);
    }
}
