package com.challenge.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Solicitation System API",
                version = "1.0.0",
                description = """
            API para gerenciamento de solicitações de atendimento.
            
            ## Fluxo de trabalho:
            1. **Cliente** se registra e cria solicitações em 3 etapas
            2. **Cliente** submete a solicitação para análise
            3. **Analista** (com cobertura por UF) analisa e decide
            4. **Admin** gerencia usuários e coberturas
            
            ## Autenticação:
            Use o endpoint `/auth/login` para obter um token JWT.
            Em seguida, inclua o token no header: `Authorization: Bearer {token}`
            """,
                contact = @Contact(
                        name = "Backend Challenge",
                        email = "challenge@example.com",
                        url = "https://github.com/seu-usuario/backend-solicitations"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                ),
                termsOfService = "https://example.com/terms"
        ),
        servers = {
                @Server(
                        url = "/",
                        description = "Default Server URL"
                )
        }
)
@SecurityScheme(
        name = "bearer-jwt",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT token obtido via /auth/login"
)
public class OpenApiConfig {
}