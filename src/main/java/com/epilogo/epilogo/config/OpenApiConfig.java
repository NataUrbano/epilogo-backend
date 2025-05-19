package com.epilogo.epilogo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Epilogo API",
                version = "1.0",
                description = "API para la gestión y reserva de libros de una biblioteca digital online. " +
                        "Epilogo permite a los usuarios buscar, reservar y gestionar préstamos de libros digitales " +
                        "de manera fácil y rápida.",
                contact = @Contact(
                        name = "Equipo de Desarrollo de Epilogo",
                        email = "soporte@epilogo.com",
                        url = "https://www.epilogo.com/soporte"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                )
        ),
        servers = {
                @Server(
                        url = "https://api.epilogo.com",
                        description = "Servidor de Producción"
                ),
                @Server(
                        url = "https://staging.epilogo.com",
                        description = "Servidor de Staging"
                ),
                @Server(
                        url = "http://localhost:8080",
                        description = "Servidor de Desarrollo Local"
                )
        },
        security = @SecurityRequirement(name = "bearerAuth"),
        tags = {
                @Tag(name = "Autenticación", description = "Operaciones relacionadas con autenticación y gestión de sesiones"),
                @Tag(name = "Usuarios", description = "Gestión de usuarios del sistema"),
                @Tag(name = "Libros", description = "Operaciones sobre el catálogo de libros"),
                @Tag(name = "Categorías", description = "Gestión de categorías de libros"),
                @Tag(name = "Autores", description = "Información y gestión de autores"),
                @Tag(name = "Reservas", description = "Gestión de reservas y préstamos de libros"),
                @Tag(name = "Admin", description = "Operaciones administrativas del sistema")
        }
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                type = HTTP,
                scheme = "bearer",
                bearerFormat = "JWT",
                description = "Token JWT de autorización. Ejemplo: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
})
public class OpenApiConfig {

        @Bean
        public OpenAPI customOpenAPI() {
                return new OpenAPI()
                        .components(new Components())
                        .info(new io.swagger.v3.oas.models.info.Info()
                                .title("Epilogo - Biblioteca Digital API")
                                .version("1.0")
                                .description("API para gestión y reserva de libros de biblioteca digital"));
        }
}