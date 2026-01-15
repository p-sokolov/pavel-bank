package com.example.bank_app.accounts.api;

import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.Currency;

import java.time.LocalDate;
import java.util.UUID;

public class CreateAccountRequest {
    public UUID userId;
    public AccountType type;
    public Currency currency;
    public LocalDate depositEndDate;

    public CreateAccountRequest() {
    }

    public CreateAccountRequest(UUID userId, AccountType type, Currency currency, LocalDate depositEndDate) {
        this.userId = userId;
        this.type = type;
        this.currency = currency;
        this.depositEndDate = depositEndDate;
    }
}
