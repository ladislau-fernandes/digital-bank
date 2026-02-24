package com.digitalbank.service;

import com.digitalbank.dto.request.TransactionRequest;
import com.digitalbank.dto.response.TransactionResponse;
import com.digitalbank.entity.Account;
import com.digitalbank.entity.Transaction;
import com.digitalbank.entity.User;
import com.digitalbank.enums.AccountStatus;
import com.digitalbank.enums.TransactionType;
import com.digitalbank.exception.BusinessException;
import com.digitalbank.exception.InsufficientFundsException;
import com.digitalbank.kafka.producer.TransactionEventProducer;
import com.digitalbank.repository.TransactionRepository;
import com.digitalbank.service.impl.AccountService;
import com.digitalbank.service.impl.S3Service;
import com.digitalbank.service.impl.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService - Testes Unitários")
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountService accountService;
    @Mock private TransactionEventProducer eventProducer;
    @Mock private S3Service s3Service;

    @InjectMocks private TransactionService transactionService;

    private User user;
    private Account account;
    private UUID accountId;

    @BeforeEach
    void setUp() {
        accountId = UUID.randomUUID();
        user = spy(User.builder().build());
        UUID userId = UUID.randomUUID();
        doReturn(userId).when(user).getId();

        account = Account.builder()
                .id(accountId).user(user)
                .accountNumber("12345678-9")
                .balance(new BigDecimal("1000.00"))
                .status(AccountStatus.ACTIVE)
                .build();

        when(accountService.findAccountById(accountId)).thenReturn(account);
    }

    @Test
    @DisplayName("Deve realizar depósito com sucesso")
    void shouldDepositSuccessfully() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("500.00"), TransactionType.DEPOSIT, null, "Depósito");
        Transaction savedTransaction = Transaction.builder().id(UUID.randomUUID()).targetAccount(account)
                .amount(new BigDecimal("500.00")).type(TransactionType.DEPOSIT).build();

        when(transactionRepository.save(any())).thenReturn(savedTransaction);
        when(transactionRepository.findById(any())).thenReturn(Optional.of(savedTransaction));
        doNothing().when(s3Service).saveTransactionReceipt(any());
        doNothing().when(eventProducer).sendTransactionEvent(any());

        TransactionResponse response = transactionService.execute(accountId, request, user);

        assertThat(response).isNotNull();
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("1500.00"));
    }

    @Test
    @DisplayName("Deve realizar saque com sucesso")
    void shouldWithdrawalSuccessfully() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("300.00"), TransactionType.WITHDRAWAL, null, "Saque");
        Transaction savedTransaction = Transaction.builder().id(UUID.randomUUID()).sourceAccount(account)
                .amount(new BigDecimal("300.00")).type(TransactionType.WITHDRAWAL).build();

        when(transactionRepository.save(any())).thenReturn(savedTransaction);
        when(transactionRepository.findById(any())).thenReturn(Optional.of(savedTransaction));
        doNothing().when(s3Service).saveTransactionReceipt(any());
        doNothing().when(eventProducer).sendTransactionEvent(any());

        TransactionResponse response = transactionService.execute(accountId, request, user);

        assertThat(response).isNotNull();
        assertThat(account.getBalance()).isEqualByComparingTo(new BigDecimal("700.00"));
    }

    @Test
    @DisplayName("Deve lançar InsufficientFundsException ao sacar sem saldo")
    void shouldThrowInsufficientFundsOnWithdrawal() {
        TransactionRequest request = new TransactionRequest(new BigDecimal("5000.00"), TransactionType.WITHDRAWAL, null, null);

        assertThatThrownBy(() -> transactionService.execute(accountId, request, user))
                .isInstanceOf(InsufficientFundsException.class);
    }

    @Test
    @DisplayName("Deve lançar exceção ao transferir para a mesma conta")
    void shouldThrowExceptionWhenTransferToSameAccount() {
        Account sameAccount = Account.builder().id(accountId).accountNumber(account.getAccountNumber()).status(AccountStatus.ACTIVE).build();
        TransactionRequest request = new TransactionRequest(new BigDecimal("100.00"), TransactionType.TRANSFER, account.getAccountNumber(), null);
        when(accountService.findAccountByNumber(account.getAccountNumber())).thenReturn(sameAccount);

        assertThatThrownBy(() -> transactionService.execute(accountId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Não é possível transferir para a mesma conta.");
    }

    @Test
    @DisplayName("Deve lançar exceção ao operar em conta bloqueada")
    void shouldThrowExceptionWhenAccountIsBlocked() {
        account.setStatus(AccountStatus.BLOCKED);
        TransactionRequest request = new TransactionRequest(new BigDecimal("100.00"), TransactionType.DEPOSIT, null, null);

        assertThatThrownBy(() -> transactionService.execute(accountId, request, user))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Conta não está ativa para operações.");
    }
}
