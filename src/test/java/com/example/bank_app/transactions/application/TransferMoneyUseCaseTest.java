package com.example.bank_app.transactions.application;

import com.example.bank_app.accounts.application.AccountRepository;
import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.transactions.domain.DepositTask;
import com.example.bank_app.transactions.domain.Transaction;
import com.example.bank_app.users.domain.User;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TransferMoneyUseCaseTest {

    @Test
    void transfer_rejectsSameAccount() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        TransactionRepository txRepo = mock(TransactionRepository.class);
        DepositTaskRepository depositRepo = mock(DepositTaskRepository.class);

        TransferMoneyUseCase uc = new TransferMoneyUseCase(accountRepository, txRepo, depositRepo);

        UUID id = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class,
                () -> uc.transfer(id, id, new BigDecimal("1")));

        verifyNoInteractions(accountRepository, txRepo, depositRepo);
    }

    @Test
    void transfer_readsAccounts_savesTransaction_andCreatesDepositTask() {
        AccountRepository accountRepository = mock(AccountRepository.class);
        TransactionRepository txRepo = mock(TransactionRepository.class);
        DepositTaskRepository depositRepo = mock(DepositTaskRepository.class);

        UUID fromId = UUID.randomUUID();
        UUID toId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("12.34");

        User owner = new User("Alice");
        Account fromAccount = new Account(owner, AccountType.CARD, Currency.RUB, null);
        Account toAccount = new Account(owner, AccountType.CARD, Currency.RUB, null);

        fromAccount.credit(new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(fromId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toId)).thenReturn(Optional.of(toAccount));
        when(txRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        when(depositRepo.save(any(DepositTask.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferMoneyUseCase uc = new TransferMoneyUseCase(accountRepository, txRepo, depositRepo);

        uc.transfer(fromId, toId, amount);

        verify(accountRepository).findByIdForUpdate(fromId);
        verify(accountRepository).findById(toId);
        verify(txRepo).save(any(Transaction.class));
        verify(depositRepo).save(any(DepositTask.class));
    }
}