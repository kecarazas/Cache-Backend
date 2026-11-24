package cl.cache.cache_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(
        title = "Cache API",
        version = "v1",
        description = "API REST de la tienda Cache: autenticación, usuarios, categorías, productos, compras y reportes. "
                + "Para probar endpoints protegidos: ejecuta POST /api/v1/auth/login, copia el accessToken y presiona Authorize."))
@SecurityScheme(name = OpenApiConfig.BEARER, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
    public static final String BEARER = "bearerAuth";
}
