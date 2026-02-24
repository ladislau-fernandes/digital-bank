package com.digitalbank.dto.response;
import com.digitalbank.entity.Account;
import com.digitalbank.enums.AccountStatus;
import com.digitalbank.enums.AccountType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
public record AccountResponse(UUID id, String accountNumber, String agency, BigDecimal balance, AccountType accountType, AccountStatus status, LocalDateTime createdAt) {
    public static AccountResponse from(Account a) {
        return new AccountResponse(a.getId(), a.getAccountNumber(), a.getAgency(), a.getBalance(), a.getAccountType(), a.getStatus(), a.getCreatedAt());
    }
}
