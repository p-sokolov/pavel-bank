package com.example.bank_app.accounts.api;

import com.example.bank_app.accounts.application.AccountCashService;
import com.example.bank_app.common.api.AmountRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountCashController {

    private final AccountCashService cashService;

    public AccountCashController(AccountCashService cashService) {
        this.cashService = cashService;
    }

    @PostMapping("/{id}/deposit")
    public void deposit(@PathVariable UUID id, @RequestBody AmountRequest request) {
        cashService.deposit(id, request.amount());
    }

    @PostMapping("/{id}/withdraw")
    public void withdraw(@PathVariable UUID id, @RequestBody AmountRequest request) {
        cashService.withdraw(id, request.amount());
    }
}
