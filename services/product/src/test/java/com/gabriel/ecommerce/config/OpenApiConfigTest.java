package com.gabriel.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void shouldBuildProductOpenApiDefinition() {
        OpenAPI openAPI = openApiConfig.productOpenApi("product-service");

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Product Service API");
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("product-service");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }
}
