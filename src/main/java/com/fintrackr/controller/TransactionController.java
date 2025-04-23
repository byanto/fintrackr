package com.fintrackr.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fintrackr.dto.ErrorResponse;
import com.fintrackr.dto.TransactionRequest;
import com.fintrackr.dto.TransactionResponse;
import com.fintrackr.model.Product;
import com.fintrackr.model.Transaction;
import com.fintrackr.service.ProductService;
import com.fintrackr.service.TransactionService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final ProductService productService;

    @GetMapping
    public List<TransactionResponse> getAllTransactions() {
        return transactionService.getAllTransactions()
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable Long id) {
        try {
            return Optional.of(transactionService.getTransactionById(id))
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception ex) {       
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest request) {
        try {
            Transaction savedTransaction = transactionService.createTransaction(toTransaction(request));
            
            // Build and return the Response Entity
            return ResponseEntity.ok(toResponse(savedTransaction));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ErrorResponse(ex.getMessage()));
        }     
    }

    /* Transaction should not be updated
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable Long id, @RequestBody TransactionRequest request) {
       return null;    
    }
    */

    private TransactionResponse toResponse(Transaction transaction) {
        return TransactionResponse.builder()
            .id(transaction.getId())
            .type(transaction.getType())
            .quantity(transaction.getQuantity())
            .productId(transaction.getProduct().getId())
            .build();
    }

    private Transaction toTransaction(TransactionRequest request) {
        // Find product by ID
        Product product = productService.getProductById(request.getProductId());

        return Transaction.builder()
            .type(request.getType())
            .quantity(request.getQuantity())
            .timestamp(LocalDateTime.now())
            .product(product)
            .build();
    }

}
