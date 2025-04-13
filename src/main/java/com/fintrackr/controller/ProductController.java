package com.fintrackr.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintrackr.dto.ProductRequest;
import com.fintrackr.dto.ProductResponse;
import com.fintrackr.model.Product;
import com.fintrackr.service.ProductService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

	private final ProductService productService;	
	
	@GetMapping
	public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts()
                .stream()
                .map(product -> toResponse(product))
                .collect(Collectors.toList());
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productService.getProductById(id)
                .map(product -> ResponseEntity.ok(toResponse(product)))
                .orElse(ResponseEntity.notFound().build());	
	}

	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {       
		Product newProduct = productService.createProduct(toProduct(request));		
        return ResponseEntity.ok(toResponse(newProduct));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        try {
            Product updatedProduct = productService.updateProduct(id, toProduct(request));
            return ResponseEntity.ok(toResponse(updatedProduct));
		} catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
		}
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
		try {
			productService.deleteProduct(id);
			return ResponseEntity.noContent().build();
		} catch (RuntimeException ex) {
			return ResponseEntity.notFound().build();
		}
	}

    // Helper method to convert Product entity to ProductResponse
	private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .stock(product.getStock())
            .build();
	}

    // Helper method to convert ProductRequest to Product
    private Product toProduct(ProductRequest request) {
        return Product.builder()
            .name(request.getName())
            .stock(request.getStock())
            .build();
    }
}
