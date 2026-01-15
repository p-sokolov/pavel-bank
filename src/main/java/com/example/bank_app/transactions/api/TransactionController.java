package com.example.bank_app.transactions.api;

import com.example.bank_app.transactions.application.TransactionQueryService;
import com.example.bank_app.transactions.application.TransferMoneyUseCase;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransferMoneyUseCase transferMoneyUseCase;
    private final TransactionQueryService queryService;

    public TransactionController(TransferMoneyUseCase transferMoneyUseCase, TransactionQueryService queryService) {
        this.transferMoneyUseCase = transferMoneyUseCase;
        this.queryService = queryService;
    }

    @PostMapping("/transfer")
    public TransactionResponse transfer(@RequestBody TransferRequest request) {
        return TransactionResponse.from(
                transferMoneyUseCase.transfer(request.from(), request.to(), request.amount())
        );
    }

    @GetMapping("/{id}")
    public TransactionResponse getById(@PathVariable UUID id) {
        return TransactionResponse.from(queryService.getById(id));
    }

    @GetMapping
    public List<TransactionResponse> listByAccount(@RequestParam UUID accountId) {
        return queryService.getByAccountId(accountId)
                .stream()
                .map(TransactionResponse::from)
                .collect(Collectors.toList());
    }
}
