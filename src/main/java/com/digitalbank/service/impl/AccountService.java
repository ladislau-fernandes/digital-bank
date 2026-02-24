package com.digitalbank.service.impl;

import com.digitalbank.dto.request.CreateAccountRequest;
import com.digitalbank.dto.response.AccountResponse;
import com.digitalbank.entity.Account;
import com.digitalbank.entity.User;
import com.digitalbank.enums.AccountStatus;
import com.digitalbank.exception.BusinessException;
import com.digitalbank.exception.ResourceNotFoundException;
import com.digitalbank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponse createAccount(User user, CreateAccountRequest request) {
        log.info("Criando conta | userId={} | tipo={}", user.getId(), request.accountType());

        Account account = Account.builder()
                .user(user)
                .accountNumber(generateAccountNumber())
                .accountType(request.accountType())
                .build();

        accountRepository.save(account);
        log.info("Conta criada com sucesso | accountId={} | number={}", account.getId(), account.getAccountNumber());
        return AccountResponse.from(account);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getUserAccounts(User user) {
        return accountRepository.findAllByUser(user)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "accounts", key = "#accountId")
    public AccountResponse getAccountById(UUID accountId, User user) {
        log.debug("Buscando conta no banco (cache miss) | accountId={}", accountId);
        Account account = findAccountById(accountId);
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Você não tem permissão para acessar esta conta.");
        }
        return AccountResponse.from(account);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountId")
    public AccountResponse blockAccount(UUID accountId, User user) {
        Account account = findAccountById(accountId);
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Você não tem permissão para bloquear esta conta.");
        }
        if (account.getStatus() == AccountStatus.CLOSED) {
            throw new BusinessException("Conta encerrada não pode ser bloqueada.");
        }
        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
        log.info("Conta bloqueada | accountId={}", accountId);
        return AccountResponse.from(account);
    }

    @Transactional
    @CacheEvict(value = "accounts", key = "#accountId")
    public AccountResponse unblockAccount(UUID accountId, User user) {
        Account account = findAccountById(accountId);
        if (!account.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Você não tem permissão para desbloquear esta conta.");
        }
        account.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        log.info("Conta desbloqueada | accountId={}", accountId);
        return AccountResponse.from(account);
    }

    public Account findAccountById(UUID accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada com ID: " + accountId));
    }

    public Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: " + accountNumber));
    }

    private String generateAccountNumber() {
        Random random = new Random();
        String number;
        do {
            number = String.format("%08d-%01d", random.nextInt(99999999), random.nextInt(9));
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }
}
