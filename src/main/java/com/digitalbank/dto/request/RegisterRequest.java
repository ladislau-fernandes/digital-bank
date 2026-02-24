package com.digitalbank.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Nome é obrigatório.")
        @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres.")
        String name,

        @NotBlank(message = "CPF é obrigatório.")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve estar no formato 000.000.000-00")
        String cpf,

        @NotBlank(message = "Email é obrigatório.")
        @Email(message = "Email inválido.")
        String email,

        @NotBlank(message = "Senha é obrigatória.")
        @Size(min = 8, message = "Senha deve ter pelo menos 8 caracteres.")
        String password
) {}
