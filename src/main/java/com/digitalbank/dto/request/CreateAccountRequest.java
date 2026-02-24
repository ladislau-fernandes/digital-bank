package com.digitalbank.dto.request;
import com.digitalbank.enums.AccountType;
import jakarta.validation.constraints.NotNull;
public record CreateAccountRequest(
        @NotNull(message = "Tipo de conta é obrigatório.") AccountType accountType
) {}
