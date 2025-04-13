package com.fintrackr.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fintrackr.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
