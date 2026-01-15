package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class AccountOperationService {

    private final AccountRepository repository;

    public AccountOperationService(AccountRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public Currency transfer(UUID fromId, UUID toId, BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Same account");
        }

        // Avoid deadlocks with pessimistic locks by locking in deterministic order.
        UUID first = fromId.compareTo(toId) <= 0 ? fromId : toId;
        UUID second = first.equals(fromId) ? toId : fromId;

        Account firstAcc = repository.findByIdForUpdate(first)
                .orElseThrow(() -> new NotFoundException("Account not found: " + first));
        Account secondAcc = repository.findByIdForUpdate(second)
                .orElseThrow(() -> new NotFoundException("Account not found: " + second));

        Account from = firstAcc.getId().equals(fromId) ? firstAcc : secondAcc;
        Account to = from == firstAcc ? secondAcc : firstAcc;

        if (from.getCurrency() != to.getCurrency()) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        from.debit(amount);
        to.credit(amount);

        return from.getCurrency();
    }
}
