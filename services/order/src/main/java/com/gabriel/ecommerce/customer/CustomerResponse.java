package com.gabriel.ecommerce.customer;

import java.util.UUID;

public record CustomerResponse(
        String id,
        String firstName,
        String lastName,
        String email
) {
}
