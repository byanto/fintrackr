package com.fintrackr.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintrackr.dto.TransactionRequest;
import com.fintrackr.dto.TransactionResponse;
import com.fintrackr.model.Product;
import com.fintrackr.model.Transaction;
import com.fintrackr.repository.ProductRepository;
import com.fintrackr.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionRequest request) {
        // Find product by ID
        Product product = productRepository.findById(request.getProductId())
                            .orElseThrow(() -> new RuntimeException("Product not found"));

        int currentStock = product.getStock();

        // Check the type of transaction and update stock accordingly
        if (request.getType().equalsIgnoreCase("IN")) {
            currentStock += request.getQuantity();            
        } else if (request.getType().equalsIgnoreCase("OUT")) {
            if (currentStock < request.getQuantity()) {
                return ResponseEntity.badRequest().build();
            }
            currentStock -= request.getQuantity();
        } else {
            return ResponseEntity.badRequest().build();
        }

        // Update the product stock and save the transaction
        product.setStock(currentStock);
        productRepository.save(product);

        // Create and save the transaction
        Transaction transaction = Transaction.builder()
                .product(product)
                .quantity(request.getQuantity())
                .type(request.getType())
                .timestamp(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        
        // Transaction successfully created and saved
        TransactionResponse response = TransactionResponse.builder()
                .id(saved.getId())
                .type(saved.getType())
                .quantity(saved.getQuantity())
                .timestamp(saved.getTimestamp())
                .productId(saved.getProduct().getId())
                .productName(saved.getProduct().getName())
                .build();

        // Build and return the response entity
        return ResponseEntity.ok(response);
    }

    public List<TransactionResponse> getAllTransactions() {
        // Retrieve all transactions and map them to TransactionResponse
        return transactionRepository.findAll().stream()
                .map(transaction -> TransactionResponse.builder()
                                        .id(transaction.getId())
                                        .type(transaction.getType())
                                        .quantity(transaction.getQuantity())
                                        .timestamp(transaction.getTimestamp())
                                        .productId(transaction.getProduct().getId())
                                        .productName(transaction.getProduct().getName())
                                        .build())
                .collect(Collectors.toList());
        
    }

}
