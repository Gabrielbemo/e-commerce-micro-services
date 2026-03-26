package com.gabriel.ecommerce.order;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order lifecycle and orchestration endpoints")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(
            summary = "Create order",
            description = "Creates an order, validates customer and product information, and triggers payment workflow."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order created",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"018fbe57-cd79-7b3f-94b1-9bbf1ff6e9cb\""))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Business or validation error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"The order should contains at least one product\""))
            )
    })
    public ResponseEntity<UUID> createOrder(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Order payload",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"reference\":\"ORDER-001\",\"amount\":2500.00,\"paymentMethod\":\"CREDIT_CARD\",\"customerId\":\"661f6f96cc6ce15f7fd8c5ba\",\"productPurchaseRequests\":[{\"productId\":\"018d2f1a-c101-7123-8234-a1b2c3d4e5f6\",\"quantity\":1}]}"
                            )
                    )
            )
            @RequestBody @Valid OrderRequest request
    ) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    @GetMapping
    @Operation(
            summary = "List orders",
            description = "Returns all orders with customer and payment metadata."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Orders listed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[{\"id\":\"018fbe57-cd79-7b3f-94b1-9bbf1ff6e9cb\",\"reference\":\"ORDER-001\",\"amount\":2500.00,\"paymentMethod\":\"CREDIT_CARD\",\"customerId\":\"661f6f96cc6ce15f7fd8c5ba\"}]"))
    )
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{orderId}")
    @Operation(
            summary = "Get order by id",
            description = "Fetches one order by id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"id\":\"018fbe57-cd79-7b3f-94b1-9bbf1ff6e9cb\",\"reference\":\"ORDER-001\",\"amount\":2500.00,\"paymentMethod\":\"CREDIT_CARD\",\"customerId\":\"661f6f96cc6ce15f7fd8c5ba\"}"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"No order found with id:: 018fbe57-cd79-7b3f-94b1-9bbf1ff6e9cb\""))
            )
    })
    public ResponseEntity<OrderResponse> findById(
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(orderService.findById(orderId));
    }
}
