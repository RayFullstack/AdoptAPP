package com.adoptapp.adoptionservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;

public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Adoption Service")
                        .description("Servidor de manejo de adopciones")
                        .version("1.0.0")
                        .license(new License().name("Uso educativo")));

    }
}
