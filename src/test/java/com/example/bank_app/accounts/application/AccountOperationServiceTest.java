package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountOperationServiceTest {

    private static Account acc(UUID id, Currency c) {
        Account a = new Account(new User("U"), AccountType.CARD, c, null);
        // set id via reflection (entity id is generated in real life)
        try {
            var f = Account.class.getDeclaredField("id");
            f.setAccessible(true);
            f.set(a, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return a;
    }

    @Test
    void transfer_rejectsSameAccount() {
        AccountRepository repo = mock(AccountRepository.class);
        AccountOperationService svc = new AccountOperationService(repo);
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> svc.transfer(id, id, new BigDecimal("1")));
        verifyNoInteractions(repo);
    }

    @Test
    void transfer_locksInDeterministicOrder_andMovesMoney() {
        AccountRepository repo = mock(AccountRepository.class);
        AccountOperationService svc = new AccountOperationService(repo);

        UUID aId = UUID.fromString("00000000-0000-0000-0000-00000000000a");
        UUID bId = UUID.fromString("00000000-0000-0000-0000-00000000000b");

        Account from = acc(bId, Currency.RUB);
        Account to = acc(aId, Currency.RUB);
        from.credit(new BigDecimal("100"));

        // fromId > toId, so service should lock aId first, then bId
        when(repo.findByIdForUpdate(aId)).thenReturn(Optional.of(to));
        when(repo.findByIdForUpdate(bId)).thenReturn(Optional.of(from));

        Currency currency = svc.transfer(bId, aId, new BigDecimal("10"));
        assertEquals(Currency.RUB, currency);
        assertEquals(new BigDecimal("90"), from.getBalance());
        assertEquals(new BigDecimal("10"), to.getBalance());

        InOrder inOrder = inOrder(repo);
        inOrder.verify(repo).findByIdForUpdate(aId);
        inOrder.verify(repo).findByIdForUpdate(bId);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void transfer_throwsNotFoundWhenMissing() {
        AccountRepository repo = mock(AccountRepository.class);
        AccountOperationService svc = new AccountOperationService(repo);
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        when(repo.findByIdForUpdate(any())).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> svc.transfer(a, b, new BigDecimal("1")));
    }

    @Test
    void transfer_rejectsCurrencyMismatch() {
        AccountRepository repo = mock(AccountRepository.class);
        AccountOperationService svc = new AccountOperationService(repo);

        UUID aId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        Account a = acc(aId, Currency.RUB);
        Account b = acc(bId, Currency.USD);
        a.credit(new BigDecimal("10"));

        when(repo.findByIdForUpdate(aId)).thenReturn(Optional.of(a));
        when(repo.findByIdForUpdate(bId)).thenReturn(Optional.of(b));

        assertThrows(IllegalArgumentException.class, () -> svc.transfer(aId, bId, new BigDecimal("1")));
    }
}
