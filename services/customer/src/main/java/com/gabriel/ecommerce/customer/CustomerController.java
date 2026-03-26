package com.gabriel.ecommerce.customer;

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

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer registration and account management")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(
            summary = "Create customer",
            description = "Registers a new customer profile with contact and address information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer created",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"661f6f96cc6ce15f7fd8c5ba\""))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"errors\":{\"email\":\"Invalid email\"}}"))
            )
    })
    public ResponseEntity<String> createCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Customer payload",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"address\":{\"street\":\"Main St\",\"houseNumber\":\"100\",\"zipCode\":\"12345\"}}")
                    )
            )
            @RequestBody @Valid CustomerRequest customer
    ) {
        return ResponseEntity.ok(customerService.createCustomer(customer));
    }

    @PutMapping
    @Operation(
            summary = "Update customer",
            description = "Updates an existing customer profile by id."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Customer updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"errors\":{\"firstName\":\"First name cannot be null\"}}"))
            )
    })
    public ResponseEntity<Void> updateCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Customer payload with id",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"id\":\"661f6f96cc6ce15f7fd8c5ba\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"address\":{\"street\":\"Main St\",\"houseNumber\":\"100\",\"zipCode\":\"12345\"}}")
                    )
            )
            @RequestBody @Valid CustomerRequest customer
    ) {
        customerService.updateCustomer(customer);
        return ResponseEntity.accepted().build();
    }

    @GetMapping
    @Operation(
            summary = "List customers",
            description = "Returns every registered customer."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Customers listed",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "[{\"id\":\"661f6f96cc6ce15f7fd8c5ba\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"address\":{\"street\":\"Main St\",\"houseNumber\":\"100\",\"zipCode\":\"12345\"}}]"))
    )
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        return ResponseEntity.ok(customerService.findAllCustomers());
    }

    @GetMapping("exists/{customerId}")
    @Operation(
            summary = "Check customer existence",
            description = "Validates whether a customer id exists in the system."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Existence checked",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "true"))
    )
    public ResponseEntity<Boolean> customerExists(
            @PathVariable String customerId
    ) {
        return ResponseEntity.ok(customerService.existsById(customerId));
    }

    @GetMapping("{customerId}")
    @Operation(
            summary = "Get customer by id",
            description = "Fetches one customer by id."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"id\":\"661f6f96cc6ce15f7fd8c5ba\",\"firstName\":\"John\",\"lastName\":\"Doe\",\"email\":\"john.doe@example.com\",\"address\":{\"street\":\"Main St\",\"houseNumber\":\"100\",\"zipCode\":\"12345\"}}"))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"No customer found with id:: 661f6f96cc6ce15f7fd8c5ba\""))
            )
    })
    public ResponseEntity<CustomerResponse> findById(
            @PathVariable String customerId
    ) {
        return ResponseEntity.ok(customerService.findById(customerId));
    }

    @DeleteMapping("{customerId}")
    @Operation(
            summary = "Delete customer",
            description = "Removes a customer by id from the customer database."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Customer deleted"),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "\"No customer found with id:: 661f6f96cc6ce15f7fd8c5ba\""))
            )
    })
    public ResponseEntity<Void> deleteCustomer(
            @PathVariable String customerId
    ) {
        customerService.deleteCustomer(customerId);
        return ResponseEntity.noContent().build();
    }
}
