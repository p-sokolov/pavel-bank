package com.example.bank_app.transactions.infrastructure;

import com.example.bank_app.transactions.application.TransactionRepository;
import com.example.bank_app.transactions.domain.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class JpaTransactionRepositoryAdapter implements TransactionRepository {

    private final TransactionJpaRepository jpa;

    public JpaTransactionRepositoryAdapter(TransactionJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Transaction save(Transaction tx) {
        return jpa.save(tx);
    }

    @Override
    public Optional<Transaction> findById(UUID id) {
        return jpa.findById(id);
    }

    @Override
    public List<Transaction> findByAccountId(UUID accountId) {
        return jpa.findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(accountId, accountId);
    }
}
