package com.example.bank_app.accounts.api;

import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.BlockReason;
import com.example.bank_app.accounts.domain.Currency;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        UUID ownerId,
        AccountType type,
        Currency currency,
        BigDecimal balance,
        boolean blocked,
        BlockReason blockReason,
        BigDecimal debtAmount,
        LocalDate depositEndDate
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getOwner().getId(),
                account.getType(),
                account.getCurrency(),
                account.getBalance(),
                account.isBlocked(),
                account.getBlockReason(),
                account.getDebtAmount(),
                account.getDepositEndDate()
        );
    }
}
