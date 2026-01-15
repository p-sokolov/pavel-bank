package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Optional<Account> findById(UUID id);
    Optional<Account> findByIdForUpdate(UUID id);
    List<Account> findByOwnerId(UUID ownerId);
    Account save(Account account);
}
