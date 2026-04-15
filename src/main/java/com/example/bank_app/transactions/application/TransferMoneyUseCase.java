package com.example.bank_app.transactions.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.application.AccountRepository;
import com.example.bank_app.transactions.domain.Transaction;
import com.example.bank_app.transactions.domain.DepositTask;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examplebank_app.accounts.application.AccountOperationService;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.audit.application.AuditService;

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
    public void transfer(UUID fromAccountId, UUID toAccountId, BigDecimal amount, String currency) {
        Account fromAccount = accountRepository.findByIdForUpdate(fromAccountId)
                .orElseThrow(() -> new RuntimeException("Sender account not found"));

        Account toAccount = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new RuntimeException("Receiver account not found"));

        if (!toAccount.getCurrency().equals(currency) || !fromAccount.getCurrency().equals(currency)) {
            throw new RuntimeException("Currency mismatch");
        }

        fromAccount.debit(amount);

        Transaction transaction = new Transaction();
        transaction.setFromAccountId(fromAccountId);
        transaction.setToAccountId(toAccountId);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setCreatedAt(LocalDateTime.now());
        transactionRepository.save(transaction);

        DepositTask depositTask = new DepositTask();
        depositTask.setAccountId(toAccountId);
        depositTask.setAmount(amount);
        depositTask.setStatus(DepositTask.TaskStatus.PENDING);
        depositTask.setCreatedAt(LocalDateTime.now());
        depositTaskRepository.save(depositTask);
    }
}
