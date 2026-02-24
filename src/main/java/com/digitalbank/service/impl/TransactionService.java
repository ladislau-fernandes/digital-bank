package com.digitalbank.service.impl;

import com.digitalbank.dto.request.TransactionRequest;
import com.digitalbank.dto.response.TransactionResponse;
import com.digitalbank.entity.Account;
import com.digitalbank.entity.Transaction;
import com.digitalbank.entity.User;
import com.digitalbank.enums.AccountStatus;
import com.digitalbank.enums.TransactionType;
import com.digitalbank.exception.BusinessException;
import com.digitalbank.exception.InsufficientFundsException;
import com.digitalbank.kafka.event.TransactionEvent;
import com.digitalbank.kafka.producer.TransactionEventProducer;
import com.digitalbank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final TransactionEventProducer eventProducer;
    private final S3Service s3Service;

    @Transactional
    public TransactionResponse execute(UUID accountId, TransactionRequest request, User user) {
        log.info("Iniciando transação | tipo={} | accountId={} | userId={}", request.type(), accountId, user.getId());

        Account account = accountService.findAccountById(accountId);

        if (!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Você não tem permissão para operar nesta conta.");
        }

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Conta não está ativa para operações.");
        }

        TransactionResponse response = switch (request.type()) {
            case DEPOSIT -> deposit(account, request);
            case WITHDRAWAL -> withdrawal(account, request);
            case TRANSFER, PIX -> transfer(account, request);
        };

        // Busca a transação salva para usar no S3 e Kafka
        Transaction transaction = transactionRepository.findById(response.id()).orElseThrow();

        // Salva comprovante no S3 (assíncrono, não bloqueia a transação)
        s3Service.saveTransactionReceipt(transaction);

        // Publica evento no Kafka para notificações
        eventProducer.sendTransactionEvent(toEvent(transaction));

        log.info("Transação concluída com sucesso | transactionId={} | tipo={}", response.id(), response.type());

        return response;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getStatement(UUID accountId, User user, Pageable pageable) {
        Account account = accountService.findAccountById(accountId);
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Você não tem permissão para visualizar este extrato.");
        }
        return transactionRepository.findAllByAccount(account, pageable)
                .map(TransactionResponse::from);
    }

    private TransactionResponse deposit(Account account, TransactionRequest request) {
        account.setBalance(account.getBalance().add(request.amount()));

        Transaction transaction = Transaction.builder()
                .targetAccount(account)
                .amount(request.amount())
                .type(TransactionType.DEPOSIT)
                .description(request.description() != null ? request.description() : "Depósito")
                .build();

        transactionRepository.save(transaction);
        return TransactionResponse.from(transaction);
    }

    private TransactionResponse withdrawal(Account account, TransactionRequest request) {
        validateSufficientFunds(account, request.amount());

        account.setBalance(account.getBalance().subtract(request.amount()));

        Transaction transaction = Transaction.builder()
                .sourceAccount(account)
                .amount(request.amount())
                .type(TransactionType.WITHDRAWAL)
                .description(request.description() != null ? request.description() : "Saque")
                .build();

        transactionRepository.save(transaction);
        return TransactionResponse.from(transaction);
    }

    private TransactionResponse transfer(Account sourceAccount, TransactionRequest request) {
        if (request.targetAccountNumber() == null || request.targetAccountNumber().isBlank()) {
            throw new BusinessException("Número da conta destino é obrigatório para transferência.");
        }

        Account targetAccount = accountService.findAccountByNumber(request.targetAccountNumber());

        if (sourceAccount.getAccountNumber().equals(targetAccount.getAccountNumber())) {
            throw new BusinessException("Não é possível transferir para a mesma conta.");
        }

        if (targetAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new BusinessException("Conta destino não está ativa.");
        }

        validateSufficientFunds(sourceAccount, request.amount());

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.amount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.amount()));

        Transaction transaction = Transaction.builder()
                .sourceAccount(sourceAccount)
                .targetAccount(targetAccount)
                .amount(request.amount())
                .type(request.type())
                .description(request.description() != null ? request.description() : "Transferência")
                .build();

        transactionRepository.save(transaction);
        return TransactionResponse.from(transaction);
    }

    private void validateSufficientFunds(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException();
        }
    }

    private TransactionEvent toEvent(Transaction t) {
        return new TransactionEvent(
                t.getId(),
                t.getSourceAccount() != null ? t.getSourceAccount().getId() : null,
                t.getTargetAccount() != null ? t.getTargetAccount().getId() : null,
                t.getSourceAccount() != null ? t.getSourceAccount().getAccountNumber() : null,
                t.getTargetAccount() != null ? t.getTargetAccount().getAccountNumber() : null,
                t.getAmount(),
                t.getType(),
                t.getDescription(),
                t.getCreatedAt()
        );
    }
}
