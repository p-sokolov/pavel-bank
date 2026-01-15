package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.BlockReason;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AccountDebtServiceTest {

    @Test
    void markDebt_and_repayDebt_workThroughDomain() {
        AccountRepository repo = mock(AccountRepository.class);
        AccountDebtService svc = new AccountDebtService(repo);

        Account a = new Account(new User("U"), AccountType.CARD, Currency.RUB, null);
        a.credit(new BigDecimal("50"));
        UUID id = UUID.randomUUID();
        when(repo.findByIdForUpdate(id)).thenReturn(Optional.of(a));

        svc.markDebt(id, new BigDecimal("20"));
        assertTrue(a.isBlocked());
        assertEquals(BlockReason.DEBT, a.getBlockReason());
        assertEquals(new BigDecimal("20"), a.getDebtAmount());

        // credit allowed while blocked for debt, so top up and repay
        a.credit(new BigDecimal("10"));
        svc.repayDebt(id, new BigDecimal("20"));
        assertEquals(BigDecimal.ZERO, a.getDebtAmount());
        assertFalse(a.isBlocked());
    }

    @Test
    void markDebt_throwsNotFoundWhenMissing() {
        AccountRepository repo = mock(AccountRepository.class);
        when(repo.findByIdForUpdate(any())).thenReturn(Optional.empty());
        AccountDebtService svc = new AccountDebtService(repo);
        assertThrows(NotFoundException.class, () -> svc.markDebt(UUID.randomUUID(), new BigDecimal("1")));
    }
}
