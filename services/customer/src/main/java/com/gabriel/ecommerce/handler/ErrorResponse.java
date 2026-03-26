package com.gabriel.ecommerce.handler;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

public record ErrorResponse(
        @Schema(description = "Validation errors by field")
        Map<String, String> errors
) {
}
