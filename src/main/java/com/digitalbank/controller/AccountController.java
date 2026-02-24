package com.digitalbank.controller;

import com.digitalbank.dto.request.CreateAccountRequest;
import com.digitalbank.dto.response.AccountResponse;
import com.digitalbank.entity.User;
import com.digitalbank.service.impl.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Contas", description = "Gerenciamento de contas bancárias")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    @Operation(summary = "Criar nova conta bancária")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request, @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(user, request));
    }

    @GetMapping
    @Operation(summary = "Listar minhas contas")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.getUserAccounts(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhe da conta (com cache Redis)")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.getAccountById(id, user));
    }

    @PatchMapping("/{id}/block")
    @Operation(summary = "Bloquear conta")
    public ResponseEntity<AccountResponse> blockAccount(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.blockAccount(id, user));
    }

    @PatchMapping("/{id}/unblock")
    @Operation(summary = "Desbloquear conta")
    public ResponseEntity<AccountResponse> unblockAccount(@PathVariable UUID id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.unblockAccount(id, user));
    }
}
