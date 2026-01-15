package com.example.bank_app.transactions.application;

import com.example.bank_app.transactions.domain.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction tx);
    Optional<Transaction> findById(UUID id);
    List<Transaction> findByAccountId(UUID accountId);
}
