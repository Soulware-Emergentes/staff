package lat.soulware.staff.shared.interfaces.rest.docs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Describes the API in the OpenAPI document springdoc serves, including the bearer token every
 * endpoint takes. The Swagger UI reads that scheme to offer a field to paste a staff-issued token
 * into, and sends the token with every call it makes from then on.
 */
@Configuration
public class ApiDocsConfiguration {

    private static final String BEARER_TOKEN = "bearer-token";

    @Bean
    OpenAPI apiDocs() {
        SecurityScheme bearerToken = new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

        return new OpenAPI()
            .info(new Info().title("staff").description("The HTTP API of the staff directory."))
            .components(new Components().addSecuritySchemes(BEARER_TOKEN, bearerToken))
            .addSecurityItem(new SecurityRequirement().addList(BEARER_TOKEN));
    }
}
