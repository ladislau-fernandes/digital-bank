package com.digitalbank.dto.request;
import com.digitalbank.enums.TransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
public record TransactionRequest(
        @NotNull(message = "Valor é obrigatório.") @DecimalMin(value = "0.01", message = "Valor mínimo é R$ 0,01.") BigDecimal amount,
        @NotNull(message = "Tipo da transação é obrigatório.") TransactionType type,
        String targetAccountNumber,
        String description
) {}
