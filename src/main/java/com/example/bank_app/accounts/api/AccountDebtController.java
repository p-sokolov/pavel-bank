package com.example.bank_app.accounts.api;

import com.example.bank_app.accounts.application.AccountDebtService;
import com.example.bank_app.common.api.AmountRequest;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
public class AccountDebtController {

    private final AccountDebtService debtService;

    public AccountDebtController(AccountDebtService debtService) {
        this.debtService = debtService;
    }

    @PostMapping("/{id}/debt")
    public void markDebt(@PathVariable UUID id, @RequestBody AmountRequest request) {
        debtService.markDebt(id, request.amount());
    }

    @PostMapping("/{id}/repay-debt")
    public void repayDebt(@PathVariable UUID id, @RequestBody AmountRequest request) {
        debtService.repayDebt(id, request.amount());
    }
}
