package com.example.bank_app.ui;

import com.example.bank_app.accounts.application.*;
import com.example.bank_app.accounts.domain.BlockReason;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.transactions.application.TransferMoneyUseCase;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Controller
@RequestMapping("/ui")
public class UiActionsController {

    private final AccountService accountService;
    private final AccountCashService accountCashService;
    private final AccountDebtService accountDebtService;
    private final AccountOperationService accountOperationService;
    private final TransferMoneyUseCase transferMoneyUseCase;

    public UiActionsController(AccountService accountService,
                               AccountCashService accountCashService,
                               AccountDebtService accountDebtService,
                               AccountOperationService accountOperationService,
                               TransferMoneyUseCase transferMoneyUseCase) {
        this.accountService = accountService;
        this.accountCashService = accountCashService;
        this.accountDebtService = accountDebtService;
        this.accountOperationService = accountOperationService;
        this.transferMoneyUseCase = transferMoneyUseCase;
    }

    private UUID requireUser(HttpSession session) {
        return UiSession.getUserId(session).orElseThrow(() -> new NotFoundException("Not logged in"));
    }

    @PostMapping("/accounts")
    public String createAccount(@RequestParam("type") String type,
                                @RequestParam("currency") String currency,
                                @RequestParam(value = "depositEndDate", required = false) String depositEndDate,
                                HttpSession session) {
        UUID userId = requireUser(session);

        LocalDate endDate = null;
        if (depositEndDate != null && !depositEndDate.isBlank()) {
            endDate = LocalDate.parse(depositEndDate);
        }

        CreateAccountCommand cmd = new CreateAccountCommand(
                userId,
                Enum.valueOf(com.example.bank_app.accounts.domain.AccountType.class, type),
                Enum.valueOf(Currency.class, currency),
                endDate
        );

        accountService.create(cmd);
        return "redirect:/ui/dashboard";
    }

    @PostMapping("/accounts/{id}/deposit")
    public String deposit(@PathVariable("id") UUID accountId,
                          @RequestParam("amount") BigDecimal amount,
                          HttpSession session) {
        requireUser(session);
        accountCashService.deposit(accountId, amount);
        return "redirect:/ui/accounts/" + accountId;
    }

    @PostMapping("/accounts/{id}/withdraw")
    public String withdraw(@PathVariable("id") UUID accountId,
                           @RequestParam("amount") BigDecimal amount,
                           HttpSession session) {
        requireUser(session);
        accountCashService.withdraw(accountId, amount);
        return "redirect:/ui/accounts/" + accountId;
    }

    @PostMapping("/transfer")
    public String transfer(@RequestParam("from") UUID from,
                           @RequestParam("to") UUID to,
                           @RequestParam("amount") BigDecimal amount,
                           HttpSession session) {
        requireUser(session);
        transferMoneyUseCase.transfer(from, to, amount);
        return "redirect:/ui/dashboard";
    }

    @PostMapping("/accounts/{id}/block")
    public String block(@PathVariable("id") UUID accountId,
                        @RequestParam("reason") BlockReason reason,
                        HttpSession session) {
        requireUser(session);
        accountService.block(accountId, reason);
        return "redirect:/ui/accounts/" + accountId;
    }

    @PostMapping("/accounts/{id}/unblock")
    public String unblock(@PathVariable("id") UUID accountId,
                          HttpSession session) {
        requireUser(session);
        accountService.unblock(accountId);
        return "redirect:/ui/accounts/" + accountId;
    }

    @PostMapping("/accounts/{id}/debt")
    public String markDebt(@PathVariable("id") UUID accountId,
                           @RequestParam("amount") BigDecimal amount,
                           HttpSession session) {
        requireUser(session);
        accountDebtService.markDebt(accountId, amount);
        return "redirect:/ui/accounts/" + accountId;
    }

    @PostMapping("/accounts/{id}/repay-debt")
    public String repayDebt(@PathVariable("id") UUID accountId,
                            @RequestParam("amount") BigDecimal amount,
                            HttpSession session) {
        requireUser(session);
        accountDebtService.repayDebt(accountId, amount);
        return "redirect:/ui/accounts/" + accountId;
    }
}
