package com.example.bank_app.transactions.api;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        UUID from,
        UUID to,
        BigDecimal amount
) {
}
