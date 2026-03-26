package com.gabriel.ecommerce.payment;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment registration endpoints")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    @Operation(
            summary = "Create payment",
            description = "Persists payment details linked to an order and emits notification events."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment created",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"018fbe59-0f8e-74f8-844a-4eb76f37e141\""))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payload",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"timestamp\":\"2026-03-26T18:47:17.998+00:00\",\"status\":400,\"error\":\"Bad Request\"}"))
            )
    })
    public ResponseEntity<UUID> createPayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Payment payload",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"amount\":2500.00,\"paymentMethod\":\"CREDIT_CARD\",\"orderId\":\"018fbe57-cd79-7b3f-94b1-9bbf1ff6e9cb\",\"orderReference\":\"ORDER-001\",\"customer\":{\"id\":\"661f6f96cc6ce15f7fd8c5ba\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\"}}")
                    )
            )
            @RequestBody @Valid PaymentRequest request
    ){
        return ResponseEntity.ok(paymentService.createPayment(request));
    }
}
