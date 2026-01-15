package com.example.bank_app.accounts.api;

import com.example.bank_app.accounts.application.AccountQueryService;
import com.example.bank_app.accounts.application.AccountService;
import com.example.bank_app.accounts.application.CreateAccountCommand;
import com.example.bank_app.accounts.domain.BlockReason;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;
    private final AccountQueryService queryService;

    public AccountController(AccountService service, AccountQueryService queryService) {
        this.service = service;
        this.queryService = queryService;
    }

    @PostMapping
    public AccountResponse create(@RequestBody CreateAccountRequest request) {
        CreateAccountCommand cmd = new CreateAccountCommand(
                request.userId,
                request.type,
                request.currency,
                request.depositEndDate
        );
        return AccountResponse.from(service.create(cmd));
    }

    @GetMapping("/{id}")
    public AccountResponse getById(@PathVariable UUID id) {
        return AccountResponse.from(queryService.getById(id));
    }

    @GetMapping
    public List<AccountResponse> list(@RequestParam UUID userId) {
        return queryService.getByOwnerId(userId)
                .stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    @PostMapping("/{id}/block")
    public void block(@PathVariable UUID id,
                      @RequestParam BlockReason reason) {
        service.block(id, reason);
    }

    @PostMapping("/{id}/unblock")
    public void unblock(@PathVariable UUID id) {
        service.unblock(id);
    }
}
