package com.gabriel.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void shouldBuildCustomerOpenApiDefinition() {
        OpenAPI openAPI = openApiConfig.customerOpenApi("customer-service");

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Customer Service API");
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("customer-service");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }
}
