package com.example.bank_app.transactions.application;

import com.example.bank_app.accounts.application.AccountOperationService;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.audit.application.AuditService;
import com.example.bank_app.transactions.domain.Transaction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransferMoneyUseCase {

    private final AccountOperationService accountOperationService;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    public TransferMoneyUseCase(AccountOperationService accountOperationService,
                                TransactionRepository transactionRepository,
                                AuditService auditService) {
        this.accountOperationService = accountOperationService;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Transaction transfer(UUID fromId, UUID toId, BigDecimal amount) {
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Same account");
        }

        Currency currency = accountOperationService.transfer(fromId, toId, amount);

        Transaction tx = new Transaction(fromId, toId, amount, currency, LocalDateTime.now());
        transactionRepository.save(tx);

        auditService.log(
                "TRANSFER",
                "From " + fromId + " to " + toId + " amount " + amount + " " + currency
        );

        return tx;
    }
}
