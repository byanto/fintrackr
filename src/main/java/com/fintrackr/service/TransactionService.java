package com.fintrackr.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintrackr.model.Product;
import com.fintrackr.model.Transaction;
import com.fintrackr.model.TransactionType;
import com.fintrackr.repository.ProductRepository;
import com.fintrackr.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    public Transaction createTransaction(Transaction transaction) {
        Product product = transaction.getProduct();
        int currentStock = product.getStock();

        // Check the type of transaction and update stock accordingly
        if (transaction.getType() == TransactionType.IN) {
            currentStock += transaction.getQuantity();            
        } else if (transaction.getType() == TransactionType.OUT) {
            if (currentStock < transaction.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for the transaction.");                
            }
            currentStock -= transaction.getQuantity();
        } else {
            throw new IllegalArgumentException("Invalid transaction type.");
        }

        // Update the product stock
        product.setStock(currentStock);        

        // Save the updated product to the database
        productRepository.save(product);

        return transactionRepository.save(transaction);
    }

    /* 
    public Transaction updateTransaction(Long id, Transaction updatedTransaction) {
        return transactionRepository.findById(id)
            .map(transaction -> {
                transaction.setType(updatedTransaction.getType());
                transaction.setQuantity(updatedTransaction.getQuantity());
                transaction.setProduct(updatedTransaction.getProduct());
                return transactionRepository.save(transaction);
            })
            .orElseThrow(() -> new IllegalArgumentException("Transaction with id " + id + " does not exist."));
    }
    */

    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transaction with id " + id + " does not exist.");
        }
        transactionRepository.deleteById(id);
    }

}
