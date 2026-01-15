package com.example.bank_app.ui;

import com.example.bank_app.accounts.application.AccountQueryService;
import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.transactions.application.TransactionQueryService;
import com.example.bank_app.transactions.domain.Transaction;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ui/accounts")
public class UiAccountController {

    private final AccountQueryService accountQueryService;
    private final TransactionQueryService transactionQueryService;

    public UiAccountController(AccountQueryService accountQueryService,
                               TransactionQueryService transactionQueryService) {
        this.accountQueryService = accountQueryService;
        this.transactionQueryService = transactionQueryService;
    }

    @GetMapping("/{id}")
    public String account(@PathVariable("id") UUID accountId, Model model, HttpSession session) {
        // Require login (simple session check)
        UiSession.getUserId(session).orElseThrow(() -> new NotFoundException("Not logged in"));

        Account account = accountQueryService.getById(accountId);
        List<Transaction> history = transactionQueryService.getByAccountId(accountId);

        model.addAttribute("account", account);
        model.addAttribute("history", history);
        return "account";
    }
}
