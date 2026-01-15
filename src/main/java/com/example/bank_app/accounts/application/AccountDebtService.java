package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.common.api.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountDebtService {

    private final AccountRepository repository;

    public AccountDebtService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void markDebt(UUID accountId, BigDecimal amount) {
        Account account = repository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.markDebt(amount);
    }

    @Transactional
    public void repayDebt(UUID accountId, BigDecimal amount) {
        Account account = repository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        account.repayDebt(amount);
    }
}
