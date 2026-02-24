package com.digitalbank.controller;

import com.digitalbank.AbstractIntegrationTest;
import com.digitalbank.dto.request.LoginRequest;
import com.digitalbank.dto.request.RegisterRequest;
import com.digitalbank.entity.User;
import com.digitalbank.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
@DisplayName("AuthController - Testes de Integração com Testcontainers")
class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        User existingUser = User.builder()
                .name("Usuário Existente")
                .cpf("987.654.321-00")
                .email("existente@email.com")
                .password(passwordEncoder.encode("senha123!"))
                .build();
        userRepository.save(existingUser);
    }

    @Test
    @DisplayName("POST /api/auth/register - Deve registrar novo usuário com sucesso")
    void shouldRegisterNewUser() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Maria Souza", "111.222.333-44", "maria@email.com", "senha123!"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.type").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("maria@email.com"));
    }

    @Test
    @DisplayName("POST /api/auth/register - Deve retornar 400 ao registrar email duplicado")
    void shouldReturn400WhenEmailExists() throws Exception {
        RegisterRequest request = new RegisterRequest(
                "Outro Usuário", "555.666.777-88", "existente@email.com", "senha123!"
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email já cadastrado."));
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve fazer login com sucesso")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest("existente@email.com", "senha123!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/auth/login - Deve retornar 401 com credenciais inválidas")
    void shouldReturn401WithInvalidCredentials() throws Exception {
        LoginRequest request = new LoginRequest("existente@email.com", "senhaErrada!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/auth/register - Deve retornar 400 com dados inválidos")
    void shouldReturn400WithInvalidData() throws Exception {
        RegisterRequest request = new RegisterRequest("", "cpf-invalido", "email-invalido", "123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }
}
