package com.example.bank_app.transactions.application;

import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.transactions.domain.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionQueryService {

    private final TransactionRepository repository;

    public TransactionQueryService(TransactionRepository repository) {
        this.repository = repository;
    }

    public Transaction getById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Transaction not found"));
    }

    public List<Transaction> getByAccountId(UUID accountId) {
        return repository.findByAccountId(accountId);
    }
}
