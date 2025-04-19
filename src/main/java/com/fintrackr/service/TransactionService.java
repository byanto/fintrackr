package com.fintrackr.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fintrackr.model.Product;
import com.fintrackr.model.Transaction;
import com.fintrackr.model.TransactionType;
import com.fintrackr.repository.ProductRepository;
import com.fintrackr.repository.TransactionRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

/**
 * Service class responsible for managing transaction operations and related stock management.
 * Handles the creation of IN/OUT transactions while maintaining product stock integrity.
 * 
 * <p>This service ensures proper validation of transaction operations including:
 * <ul>
 *   <li>Stock availability validation for OUT transactions</li>
 *   <li>Transaction type validation</li>
 *   <li>Automatic stock updates based on transaction type</li>
 * </ul>
 * </p>
 *
 * @author Budi Yanto
 * @since 1.0
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    
    /**
     * Retrieves all transactions.
     *
     * @return a list of all transactions
     */
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    /**
     * Retrieves a transaction by its unique identifier.
     *
     * @param id the unique identifier of the transaction
     * @return the found transaction entity
     */
    public Optional<Transaction> getTransactionById(Long id) {
        return transactionRepository.findById(id);
    }

    /**
     * Creates a new transaction and updates the associated product's stock.
     * For IN transactions, increases the product stock.
     * For OUT transactions, decreases the product stock if sufficient quantity is available.
     *
     * @param transaction the transaction to be created, containing:
     *                   - transaction type (IN/OUT)
     *                   - quantity
     *                   - associated product
     * @return the saved transaction with updated product stock
     * @throws IllegalArgumentException if:
     *         - transaction type is null or invalid
     *         - insufficient stock for OUT transactions
     *         - quantity is invalid
     */
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

    /**
     * Deletes a transactions.
     *
     * @param id the unique identifier of the transaction to delete
     * @throws IllegalArgumentException if no transaction exists with the given ID
     */
    public void deleteTransaction(Long id) {
        if (!transactionRepository.existsById(id)) {
            throw new IllegalArgumentException("Transaction with id " + id + " does not exist.");
        }
        transactionRepository.deleteById(id);
    }

}
