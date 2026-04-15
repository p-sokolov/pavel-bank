package com.example.bank_app.transactions.application;

import com.example.bank_app.accounts.application.AccountRepository;
import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.transactions.domain.DepositTask;
import com.example.bank_app.transactions.domain.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferMoneyUseCase {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final DepositTaskRepository depositTaskRepository;

    public TransferMoneyUseCase(AccountRepository accountRepository,
                                TransactionRepository transactionRepository,
                                DepositTaskRepository depositTaskRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.depositTaskRepository = depositTaskRepository;
    }

    @Transactional
    public Transaction transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Sender and receiver accounts must be different");
        }

        Account fromAccount = accountRepository.findByIdForUpdate(fromAccountId)
                .orElseThrow(() -> new NotFoundException("Sender account not found"));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new NotFoundException("Receiver account not found"));

        Currency currency = fromAccount.getCurrency();
        if (currency != toAccount.getCurrency()) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        fromAccount.debit(amount);

        Transaction transaction = new Transaction(
                fromAccountId,
                toAccountId,
                amount,
                currency,
                LocalDateTime.now()
        );

        Transaction saved = transactionRepository.save(transaction);

        DepositTask depositTask = new DepositTask();
        depositTask.setAccountId(toAccountId);
        depositTask.setAmount(amount);
        depositTask.setStatus(DepositTask.TaskStatus.PENDING);
        depositTask.setCreatedAt(LocalDateTime.now());
        depositTaskRepository.save(depositTask);

        return saved;
    }
}