package com.example.bank_app.accounts.infrastructure;

import com.example.bank_app.accounts.domain.Account;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JpaAccountRepositoryTest {

    @Test
    void adapter_delegatesToSpringDataRepository() {
        AccountJpaRepository jpa = mock(AccountJpaRepository.class);
        JpaAccountRepository adapter = new JpaAccountRepository(jpa);

        UUID id = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Account a = mock(Account.class);

        when(jpa.findById(id)).thenReturn(Optional.of(a));
        when(jpa.findByIdForUpdate(id)).thenReturn(Optional.of(a));
        when(jpa.findByOwnerId(ownerId)).thenReturn(List.of(a));
        when(jpa.save(a)).thenReturn(a);

        assertEquals(Optional.of(a), adapter.findById(id));
        assertEquals(Optional.of(a), adapter.findByIdForUpdate(id));
        assertEquals(List.of(a), adapter.findByOwnerId(ownerId));
        assertSame(a, adapter.save(a));

        verify(jpa).findById(id);
        verify(jpa).findByIdForUpdate(id);
        verify(jpa).findByOwnerId(ownerId);
        verify(jpa).save(a);
    }
}
