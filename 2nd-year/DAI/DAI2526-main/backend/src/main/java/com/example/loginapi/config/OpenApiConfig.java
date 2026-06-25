package com.example.loginapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.clientes.Cliente;


/**
 * OpenAPI 3.0 configuration (SW-RNF.S.04 – Interoperabilidade Técnica).
 * Access Swagger UI at: http://localhost:8080/swagger-ui/index.html
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tubOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TUB – Transportes Urbanos de Braga API")
                .description("""
                    API REST do sistema TUB – Plataforma de transportes públicos urbanos.
                    
                    **Perfis de acesso:**
                    - `ADMIN` – Acesso total, incluindo backoffice, logs, exportação e gestão de avisos
                    - `CLIENTE` – Acesso às funcionalidades de utilizador final
                    
                    **Convenções de rotas:**
                    - `/api/login`, `/api/register` – Público
                    - `/api/user/**` – Autenticado
                    - `/api/admin/**` – ADMIN only
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("TUB Dev Team")
                    .email("dev@tub.pt"))
            );
    }
}
