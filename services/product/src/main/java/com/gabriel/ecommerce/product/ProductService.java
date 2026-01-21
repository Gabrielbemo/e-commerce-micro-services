package com.gabriel.ecommerce.product;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService {
    public UUID createProduct(@Valid ProductRequest request) {
        return null;
    }

    public List<ProductPurchaseResponse> purchaseProducts(List<ProductPurchaseRequest> productPurchaseRequestList) {
        return null;
    }
}
