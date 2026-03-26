package com.gabriel.ecommerce.product;

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
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Catalog and stock operations for products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(
            summary = "Create product",
            description = "Creates a new product with category association and initial stock."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product created successfully",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"018d2f1a-c101-7123-8234-a1b2c3d4e5f6\""))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid payload",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"errors\":{\"name\":\"Name cannot be null\"}}"))
            )
    })
    public ResponseEntity<UUID> createProduct(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Product payload",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"name\":\"Gaming Laptop\",\"description\":\"16-inch high performance laptop\",\"availableQuantity\":12,\"price\":8999.90,\"categoryId\":\"018d2f1a-c100-7000-9000-a1b2c3d4e5f6\"}"
                            )
                    )
            )
            @RequestBody @Valid ProductRequest request
    ) {
        return ResponseEntity.ok(productService.createProduct(request));
    }

    @PostMapping("/purchase")
    @Operation(
            summary = "Reserve product quantities",
            description = "Validates product availability and reserves stock for an order flow."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Stock reserved",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[{\"productId\":\"018d2f1a-c101-7123-8234-a1b2c3d4e5f6\",\"name\":\"Gaming Laptop\",\"description\":\"16-inch high performance laptop\",\"price\":8999.90,\"quantity\":1}]"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Insufficient stock or invalid request",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"Insufficient stock quantity for product with id:: 018d2f1a-c101-7123-8234-a1b2c3d4e5f6\""))
            )
    })
    public ResponseEntity<List<ProductPurchaseResponse>> purchaseProducts(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "List of products and requested quantities",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "[{\"productId\":\"018d2f1a-c101-7123-8234-a1b2c3d4e5f6\",\"quantity\":1}]")
                    )
            )
            @RequestBody List<ProductPurchaseRequest> productPurchaseRequestList
    ){
        return ResponseEntity.ok(productService.purchaseProducts(productPurchaseRequestList));
    }

    @GetMapping("/{productId}")
    @Operation(
            summary = "Get product by id",
            description = "Fetches detailed product information including category metadata."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Product found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"id\":\"018d2f1a-c101-7123-8234-a1b2c3d4e5f6\",\"name\":\"Gaming Laptop\",\"description\":\"16-inch high performance laptop\",\"availableQuantity\":11,\"price\":8999.90,\"categoryId\":\"018d2f1a-c100-7000-9000-a1b2c3d4e5f6\",\"categoryName\":\"Electronics\",\"categoryDescription\":\"Electronic devices and gadgets\"}"))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Product not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"No product found with id::018d2f1a-c101-7123-8234-a1b2c3d4e5f6\""))
            )
    })
    public ResponseEntity<ProductResponse> findById(
            @PathVariable UUID productId
    ){
        return ResponseEntity.ok(productService.findById(productId));
    }

    @GetMapping
    @Operation(
            summary = "List products",
            description = "Returns all products currently available in the catalog."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Products listed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[{\"id\":\"018d2f1a-c101-7123-8234-a1b2c3d4e5f6\",\"name\":\"Gaming Laptop\",\"description\":\"16-inch high performance laptop\",\"availableQuantity\":11,\"price\":8999.90,\"categoryId\":\"018d2f1a-c100-7000-9000-a1b2c3d4e5f6\",\"categoryName\":\"Electronics\",\"categoryDescription\":\"Electronic devices and gadgets\"}]"))
    )
    public ResponseEntity<List<ProductResponse>> findAll(){
        return ResponseEntity.ok(productService.findAll());
    }
}
