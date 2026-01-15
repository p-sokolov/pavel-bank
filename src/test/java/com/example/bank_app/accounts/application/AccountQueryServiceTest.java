package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountQueryServiceTest {

    @Test
    void getById_returnsAccountOrThrows() {
        AccountRepository repo = mock(AccountRepository.class);
        AccountQueryService svc = new AccountQueryService(repo);

        UUID id = UUID.randomUUID();
        Account a = new Account(new User("U"), AccountType.CARD, Currency.RUB, null);
        when(repo.findById(id)).thenReturn(Optional.of(a));

        assertSame(a, svc.getById(id));

        when(repo.findById(id)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> svc.getById(id));
    }

    @Test
    void getByOwnerId_delegates() {
        AccountRepository repo = mock(AccountRepository.class);
        AccountQueryService svc = new AccountQueryService(repo);
        UUID ownerId = UUID.randomUUID();
        when(repo.findByOwnerId(ownerId)).thenReturn(List.of());

        assertEquals(List.of(), svc.getByOwnerId(ownerId));
        verify(repo).findByOwnerId(ownerId);
    }
}
