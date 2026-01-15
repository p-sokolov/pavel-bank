package com.example.bank_app.ui;

import com.example.bank_app.accounts.application.AccountQueryService;
import com.example.bank_app.accounts.domain.Account;
import com.example.bank_app.accounts.domain.AccountType;
import com.example.bank_app.accounts.domain.Currency;
import com.example.bank_app.common.api.NotFoundException;
import com.example.bank_app.users.application.UserRepository;
import com.example.bank_app.users.domain.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/ui")
public class UiDashboardController {

    private final UserRepository userRepository;
    private final AccountQueryService accountQueryService;

    public UiDashboardController(UserRepository userRepository,
                                 AccountQueryService accountQueryService) {
        this.userRepository = userRepository;
        this.accountQueryService = accountQueryService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, HttpSession session) {
        UUID userId = UiSession.getUserId(session)
                .orElseThrow(() -> new NotFoundException("Not logged in"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Account> accounts = accountQueryService.getByOwnerId(userId);

        model.addAttribute("user", user);
        model.addAttribute("accounts", accounts);
        model.addAttribute("accountTypes", AccountType.values());
        model.addAttribute("currencies", Currency.values());

        return "dashboard";
    }
}
