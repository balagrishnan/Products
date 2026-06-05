package com.example.demo.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // JpaRepository gives you: findAll, findById, save, deleteById, etc.
}
