package com.example.bank_app.transactions.infrastructure;

import com.example.bank_app.transactions.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByFromAccountIdOrToAccountIdOrderByCreatedAtDesc(UUID fromAccountId, UUID toAccountId);
}
