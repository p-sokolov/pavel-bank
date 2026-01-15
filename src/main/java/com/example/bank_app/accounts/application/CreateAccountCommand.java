package com.example.bank_app.accounts.application;

import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.Currency;

import java.time.LocalDate;
import java.util.UUID;

public record CreateAccountCommand(
        UUID userId,
        AccountType type,
        Currency currency,
        LocalDate depositEndDate
) {}
