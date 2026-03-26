package com.gabriel.ecommerce.customer;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Validated
public class Address {
    @Schema(description = "Street name", example = "Main St")
    private String street;
    @Schema(description = "House number", example = "100")
    private String houseNumber;
    @Schema(description = "Zip code", example = "12345")
    private String zipCode;
}
