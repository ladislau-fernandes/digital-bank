package com.digitalbank.controller;

import com.digitalbank.dto.request.TransactionRequest;
import com.digitalbank.dto.response.TransactionResponse;
import com.digitalbank.entity.User;
import com.digitalbank.service.impl.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
@RequiredArgsConstructor
@Tag(name = "Transações", description = "Depósito, saque, transferência e PIX")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @Operation(summary = "Realizar transação (gera evento Kafka + salva comprovante no S3)")
    public ResponseEntity<TransactionResponse> executeTransaction(
            @PathVariable UUID accountId,
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(transactionService.execute(accountId, request, user));
    }

    @GetMapping
    @Operation(summary = "Extrato paginado da conta")
    public ResponseEntity<Page<TransactionResponse>> getStatement(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable
    ) {
        return ResponseEntity.ok(transactionService.getStatement(accountId, user, pageable));
    }
}
