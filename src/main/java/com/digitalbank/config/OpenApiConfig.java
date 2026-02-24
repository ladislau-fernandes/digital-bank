package com.digitalbank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Digital Bank API",
                version = "1.0",
                description = "API REST de banco digital com Spring Boot 3, Kafka, Redis e AWS S3.",
                contact = @Contact(name = "Digital Bank", email = "contato@digitalbank.com")
        )
)
@SecurityScheme(
        name = "bearerAuth",
        description = "Autenticação via JWT. Insira: Bearer {token}",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
