package com.example.usuarios.config;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Documentación OpenAPI / Swagger UI.
 * Declara el esquema Bearer (JWT) para poder cargar el token con el botón "Authorize".
 */
@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    /** Rutas protegidas por el filtro JWT (mismas que en SecurityConfig). */
    private static final String RUTAS_PROTEGIDAS = "/api/usuarios";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Registro de Usuarios")
                        .version("1.0.0")
                        .description("API RESTful de registro de usuarios (solo JSON)"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token devuelto por POST /api/registro")));
    }

    /** Marca con candado solo los endpoints que requieren token. */
    @Bean
    public OpenApiCustomizer seguridadEndpointsProtegidos() {
        return openApi -> openApi.getPaths().forEach((ruta, item) -> {
            if (ruta.startsWith(RUTAS_PROTEGIDAS)) {
                item.readOperations().forEach(op ->
                        op.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME)));
            }
        });
    }
}
