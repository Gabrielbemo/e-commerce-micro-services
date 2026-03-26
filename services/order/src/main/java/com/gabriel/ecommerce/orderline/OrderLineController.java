package com.gabriel.ecommerce.orderline;

import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/order-lines")
@RequiredArgsConstructor
@Tag(name = "Order Lines", description = "Read order item lines for existing orders")
public class OrderLineController {

    private final OrderLineService orderLineService;

    @GetMapping("/order/{orderId}")
    @Operation(
            summary = "List order lines by order id",
            description = "Returns line items associated with a specific order id."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Order lines listed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[{\"id\":\"018fbe58-25ba-7f7e-bf3b-773f9fa4e3f4\",\"quantity\":1.0}]"))
    )
    public ResponseEntity<List<OrderLineResponse>> findAllByOrderId(
            @PathVariable UUID orderId
    ) {
        return ResponseEntity.ok(orderLineService.findAllByOrderId(orderId));
    }
}
