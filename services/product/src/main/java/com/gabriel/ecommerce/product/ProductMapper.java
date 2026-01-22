package com.gabriel.ecommerce.product;

import com.gabriel.ecommerce.category.Category;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

@Service
public class ProductMapper {
    public Product toProduct(@Valid ProductRequest request) {
        return Product.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .availableQuantity(request.availableQuantity())
                .price(request.price())
                .category(Category.builder().id(request.categoryId()).build())
                .build();
    }

    public ProductResponse toProductResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getAvailableQuantity(),
                product.getPrice(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getCategory().getDescription()
        );
    }

    public ProductPurchaseResponse toProductPurchaseResponse(Product storedProduct) {
        return new ProductPurchaseResponse(
                storedProduct.getId(),
                storedProduct.getName(),
                storedProduct.getDescription(),
                storedProduct.getPrice(),
                storedProduct.getAvailableQuantity()
        );
    }
}
