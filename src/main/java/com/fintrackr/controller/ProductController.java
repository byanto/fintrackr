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
import com.fintrackr.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {

	private final ProductRepository productRepository;	
	
	@GetMapping
	public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> toResponse(product))
                .collect(Collectors.toList());
	}
	
	@PostMapping
	public ResponseEntity<ProductResponse> createProduct(@RequestBody ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
				.stock(request.getStock())
                .build();
		Product savedProduct = productRepository.save(product);		
        return ResponseEntity.ok(toResponse(savedProduct));
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(toResponse(product)))
                .orElse(ResponseEntity.notFound().build());	
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id, @RequestBody ProductRequest request) {
        return productRepository.findById(id).map(product -> {
            	product.setName(request.getName());
				product.setStock(request.getStock());
            	Product updatedProduct = productRepository.save(product);
				return ResponseEntity.ok(toResponse(updatedProduct));
        }).orElse(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        if (!productRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
		productRepository.deleteById(id);
        return ResponseEntity.noContent().build();
	}

    // Helper method to convert Product entity to ProductResponse
	private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .stock(product.getStock())
            .build();
	}
}
