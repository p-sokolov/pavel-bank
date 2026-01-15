package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.common.api.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AccountQueryService {

    private final AccountRepository repository;

    public AccountQueryService(AccountRepository repository) {
        this.repository = repository;
    }

    public Account getById(UUID accountId) {
        return repository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found"));
    }

    public List<Account> getByOwnerId(UUID ownerId) {
        return repository.findByOwnerId(ownerId);
    }
}
