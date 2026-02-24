package com.digitalbank.service.impl;

import com.digitalbank.dto.request.LoginRequest;
import com.digitalbank.dto.request.RegisterRequest;
import com.digitalbank.dto.response.AuthResponse;
import com.digitalbank.dto.response.UserResponse;
import com.digitalbank.entity.User;
import com.digitalbank.exception.BusinessException;
import com.digitalbank.repository.UserRepository;
import com.digitalbank.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("Email já cadastrado.");
        }
        if (userRepository.existsByCpf(request.cpf())) {
            throw new BusinessException("CPF já cadastrado.");
        }

        User user = User.builder()
                .name(request.name())
                .cpf(request.cpf())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        userRepository.save(user);
        log.info("Novo usuário registrado | userId={} | email={}", user.getId(), user.getEmail());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserResponse.from(user));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado."));

        log.info("Login realizado | userId={} | email={}", user.getId(), user.getEmail());

        String token = jwtService.generateToken(user);
        return new AuthResponse(token, UserResponse.from(user));
    }
}
