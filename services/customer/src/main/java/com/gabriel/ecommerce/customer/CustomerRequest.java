package com.gabriel.ecommerce.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

public record CustomerRequest(
        @Schema(description = "Customer id for updates", example = "661f6f96cc6ce15f7fd8c5ba")
        String id,
        @NotNull(message = "First name cannot be null")
        @Schema(description = "Customer first name", example = "John")
        String firstName,
        @NotNull(message = "Last name cannot be null")
        @Schema(description = "Customer last name", example = "Doe")
        String lastName,
        @NotNull(message = "Email cannot be null")
        @Email(message = "Invalid email")
        @Schema(description = "Customer email", example = "john.doe@example.com")
        String email,
        @Schema(description = "Customer address")
        Address address
) {
}
