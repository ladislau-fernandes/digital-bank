package com.digitalbank.dto.response;

import com.digitalbank.entity.User;
import com.digitalbank.enums.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(UUID id, String name, String cpf, String email, UserRole role, LocalDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getCpf(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
