package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountCashServiceTest {

    @Test
    void deposit_and_withdraw_delegateToDomain() {
        AccountRepository repo = mock(AccountRepository.class);
        AccountCashService svc = new AccountCashService(repo);

        Account a = new Account(new User("U"), AccountType.CARD, Currency.RUB, null);
        UUID id = UUID.randomUUID();
        when(repo.findByIdForUpdate(id)).thenReturn(Optional.of(a));

        svc.deposit(id, new BigDecimal("10"));
        assertEquals(new BigDecimal("10"), a.getBalance());

        svc.withdraw(id, new BigDecimal("3"));
        assertEquals(new BigDecimal("7"), a.getBalance());
    }

    @Test
    void deposit_throwsNotFoundWhenMissing() {
        AccountRepository repo = mock(AccountRepository.class);
        when(repo.findByIdForUpdate(any())).thenReturn(Optional.empty());

        AccountCashService svc = new AccountCashService(repo);
        assertThrows(NotFoundException.class, () -> svc.deposit(UUID.randomUUID(), new BigDecimal("1")));
    }
}
