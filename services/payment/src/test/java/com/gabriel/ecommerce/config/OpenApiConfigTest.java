package com.gabriel.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig openApiConfig = new OpenApiConfig();

    @Test
    void shouldBuildPaymentOpenApiDefinition() {
        OpenAPI openAPI = openApiConfig.paymentOpenApi("payment-service");

        assertThat(openAPI.getInfo().getTitle()).isEqualTo("Payment Service API");
        assertThat(openAPI.getInfo().getContact().getName()).isEqualTo("payment-service");
        assertThat(openAPI.getComponents().getSecuritySchemes()).containsKey("bearerAuth");
        assertThat(openAPI.getSecurity()).isNotEmpty();
    }
}
