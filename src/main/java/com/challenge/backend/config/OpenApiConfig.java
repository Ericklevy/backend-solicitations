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

import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.PathItem;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Solicitation System API",
                version = "1.0.0",
                description = """
            API para gerenciamento de solicitações de atendimento.
            
            ## 🔐 Como autenticar:
            1. Use `POST /auth/login` com email e senha para obter um **token JWT**
            2. Clique no botão **Authorize 🔓** no topo da página
            3. Cole o token no campo e clique em **Authorize**
            
            ## 📋 Fluxo completo de teste (ordem recomendada):
            
            ### 👤 CLIENTE
            1. `POST /auth/register` – Criar conta de cliente
            2. `POST /api/solicitations` – Criar rascunho → anote o **id** retornado
            3. `PATCH /api/solicitations/{id}/step1` – Tipo de serviço e descrição
            4. `PATCH /api/solicitations/{id}/step2` – Endereço via CEP (integra ViaCEP)
            5. `PATCH /api/solicitations/{id}/step3` – Prioridade, valor e prazo
            6. `POST /api/solicitations/{id}/submit` – Enviar para análise (indexa no Elasticsearch)
            
            ### 🛡️ ADMIN (login: admin@system.com / Admin@123)
            7. `POST /api/populate` – Popular o sistema com dados de exemplo
            8. `POST /api/admin/users` – Criar analista → anote o **id** do analista
            9. `PUT /api/admin/users/{id}/coverage` – Definir UFs que o analista cobre
            
            ### 🔍 ANALISTA (login com o analista criado acima)
            10. `GET /api/analyst/solicitations/search` – Buscar solicitações (Elasticsearch)
            11. `POST /api/analyst/solicitations/{id}/start` – Iniciar análise → **{id} = ID da solicitação**
            12. `POST /api/analyst/solicitations/{id}/decide` – Aprovar ou rejeitar
            
            ## ⚠️ Sobre o campo {id}:
            - Nos endpoints de **Solicitation e Analyst**: {id} = **ID da SOLICITAÇÃO** (retornado ao criar a solicitação)
            - Nos endpoints de **Admin**: {id} = **ID do USUÁRIO** (retornado ao criar o usuário)
            """,
                contact = @Contact(
                        name = "Backend Challenge",
                        email = "challenge@example.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
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

    @Bean
    public OpenApiCustomizer sortOperationsCustomizer() {
        return openApi -> {
            Paths paths = openApi.getPaths();
            if (paths != null) {
                LinkedHashMap<String, PathItem> sortedPaths = paths.entrySet().stream()
                        .sorted(Comparator.comparing(entry -> {
                            PathItem item = entry.getValue();
                            if (item.readOperations().isEmpty()) return "";
                            String opId = item.readOperations().get(0).getOperationId();
                            return opId != null ? opId : "";
                        }))
                        .collect(LinkedHashMap::new,
                                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                                LinkedHashMap::putAll);
                
                Paths newPaths = new Paths();
                sortedPaths.forEach(newPaths::addPathItem);
                openApi.setPaths(newPaths);
            }
        };
    }
}