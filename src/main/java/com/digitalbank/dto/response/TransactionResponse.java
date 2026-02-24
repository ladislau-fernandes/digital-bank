package com.digitalbank.dto.response;
import com.digitalbank.entity.Transaction;
import com.digitalbank.enums.TransactionStatus;
import com.digitalbank.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public record TransactionResponse(UUID id, BigDecimal amount, TransactionType type, String description, TransactionStatus status, String sourceAccountNumber, String targetAccountNumber, LocalDateTime createdAt) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(t.getId(), t.getAmount(), t.getType(), t.getDescription(), t.getStatus(),
                t.getSourceAccount() != null ? t.getSourceAccount().getAccountNumber() : null,
                t.getTargetAccount() != null ? t.getTargetAccount().getAccountNumber() : null,
                t.getCreatedAt());
    }
}
