package com.example.bank_app.transactions.application;

import com.example.bank_app.accounts.application.AccountOperationService;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.audit.application.AuditService;
import com.example.bank_app.transactions.domain.Transaction;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransferMoneyUseCaseTest {

    @Test
    void transfer_rejectsSameAccount() {
        AccountOperationService op = mock(AccountOperationService.class);
        TransactionRepository txRepo = mock(TransactionRepository.class);
        AuditService audit = mock(AuditService.class);

        TransferMoneyUseCase uc = new TransferMoneyUseCase(op, txRepo, audit);
        UUID id = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class, () -> uc.transfer(id, id, new BigDecimal("1")));
        verifyNoInteractions(op, txRepo, audit);
    }

    @Test
    void transfer_callsOperationService_persistsTransaction_andWritesAudit() {
        AccountOperationService op = mock(AccountOperationService.class);
        TransactionRepository txRepo = mock(TransactionRepository.class);
        AuditService audit = mock(AuditService.class);

        UUID from = UUID.randomUUID();
        UUID to = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("12.34");

        when(op.transfer(from, to, amount)).thenReturn(Currency.RUB);
        when(txRepo.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        TransferMoneyUseCase uc = new TransferMoneyUseCase(op, txRepo, audit);
        Transaction tx = uc.transfer(from, to, amount);

        assertEquals(from, tx.getFromAccountId());
        assertEquals(to, tx.getToAccountId());
        assertEquals(amount, tx.getAmount());
        assertEquals(Currency.RUB, tx.getCurrency());
        assertNotNull(tx.getCreatedAt());

        verify(op).transfer(from, to, amount);
        verify(txRepo).save(any(Transaction.class));

        ArgumentCaptor<String> action = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> details = ArgumentCaptor.forClass(String.class);
        verify(audit).log(action.capture(), details.capture());
        assertEquals("TRANSFER", action.getValue());
        assertTrue(details.getValue().contains(from.toString()));
        assertTrue(details.getValue().contains(to.toString()));
        assertTrue(details.getValue().contains(amount.toString()));
        assertTrue(details.getValue().contains("RUB"));
    }
}
