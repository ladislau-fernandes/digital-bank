package com.digitalbank.kafka.event;

import com.digitalbank.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionEvent(
        UUID transactionId,
        UUID sourceAccountId,
        UUID targetAccountId,
        String sourceAccountNumber,
        String targetAccountNumber,
        BigDecimal amount,
        TransactionType type,
        String description,
        LocalDateTime createdAt
) {}
