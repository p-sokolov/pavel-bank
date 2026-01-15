package com.example.bank_app.accounts.infrastructure;

import com.example.bank_app.accounts.application.AccountRepository;
import com.example.bank_app.accounts.domain.Account;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaAccountRepository implements AccountRepository {

    private final AccountJpaRepository jpa;

    public JpaAccountRepository(AccountJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Account> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public Optional<Account> findByIdForUpdate(UUID id) {
        return jpa.findByIdForUpdate(id);
    }

    @Override
    public List<Account> findByOwnerId(UUID ownerId) {
        return jpa.findByOwnerId(ownerId);
    }

    @Override
    public Account save(Account account) {
        return jpa.save(account);
    }
}
