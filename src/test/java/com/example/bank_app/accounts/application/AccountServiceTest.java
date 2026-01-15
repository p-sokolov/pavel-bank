package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.BlockReason;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.application.UserRepository;
import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Test
    void create_requiresExistingUser_andSavesAccount() {
        AccountRepository accountRepo = mock(AccountRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        AccountService service = new AccountService(accountRepo, userRepo);

        UUID userId = UUID.randomUUID();
        User user = new User("Ann");
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(accountRepo.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateAccountCommand cmd = new CreateAccountCommand(userId, AccountType.CARD, Currency.RUB, null);
        Account created = service.create(cmd);

        assertNotNull(created);
        assertEquals(AccountType.CARD, created.getType());
        assertEquals(Currency.RUB, created.getCurrency());
        assertSame(user, created.getOwner());
        assertTrue(user.getAccounts().contains(created), "Account should be added to user");
        verify(accountRepo).save(any(Account.class));
    }

    @Test
    void create_throwsNotFoundIfUserMissing() {
        AccountRepository accountRepo = mock(AccountRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        AccountService service = new AccountService(accountRepo, userRepo);

        when(userRepo.findById(any())).thenReturn(Optional.empty());
        CreateAccountCommand cmd = new CreateAccountCommand(UUID.randomUUID(), AccountType.CARD, Currency.RUB, null);
        assertThrows(NotFoundException.class, () -> service.create(cmd));
        verify(accountRepo, never()).save(any());
    }

    @Test
    void block_and_unblock_usePessimisticLock() {
        AccountRepository accountRepo = mock(AccountRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        AccountService service = new AccountService(accountRepo, userRepo);

        Account account = new Account(new User("U"), AccountType.CARD, Currency.RUB, null);
        UUID accId = UUID.randomUUID();
        when(accountRepo.findByIdForUpdate(accId)).thenReturn(Optional.of(account));

        service.block(accId, BlockReason.FRAUD);
        assertTrue(account.isBlocked());

        service.unblock(accId);
        assertFalse(account.isBlocked());

        verify(accountRepo, times(2)).findByIdForUpdate(accId);
    }

    @Test
    void block_unblock_throwNotFoundIfAccountMissing() {
        AccountRepository accountRepo = mock(AccountRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        AccountService service = new AccountService(accountRepo, userRepo);

        UUID accId = UUID.randomUUID();
        when(accountRepo.findByIdForUpdate(accId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.block(accId, BlockReason.FRAUD));
        assertThrows(NotFoundException.class, () -> service.unblock(accId));
    }
}
