package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.common.api.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountCashService {

    private final AccountRepository repository;

    public AccountCashService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void deposit(UUID accountId, BigDecimal amount) {
        Account account = repository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.credit(amount);
    }

    @Transactional
    public void withdraw(UUID accountId, BigDecimal amount) {
        Account account = repository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.debit(amount);
    }
}
