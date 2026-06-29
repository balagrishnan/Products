package com.example.demo.controller;

import lombok.AllArgsConstructor;
import com.example.demo.model.Product;
import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

// Kafka code
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.example.productapi.service.ProductEventProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    // 1. Inject Spring's KafkaTemplate to handle communication with Docker Kafka
    private final KafkaTemplate<String, String> kafkaTemplate;
    // 2. Inject an ObjectMapper to cleanly convert your Java object into a JSON String
    private final ObjectMapper objectMapper;
    private static final String TOPIC_NAME = "product_updates";

    //GET All Products
    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // GET Product By id
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody ProductDTO dto) {
        // First, execute your original database/service logic
        Product savedProduct = productService.createProduct(dto);

        // 3. Asynchronously push a copy of the product data into Kafka!
        try{
            // Convert the saved product object into a text JSON String
            String jsonPayload = objectMapper.writeValueAsString(savedProduct);

            // Send to topic "product_updates" using the product's ID as the message key
            kafkaTemplate.send(TOPIC_NAME, String.valueOf(savedProduct.getId()), jsonPayload);
            System.out.println("Event successfully sent to Kafka for Product ID: " + savedProduct.getId());

        } catch (Exception e){
            // Log the error but don't crash the web request—the database write already succeeded!
            System.err.println("Failed to publish product event to Kafka: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody ProductDTO dto) {
        // return ResponseEntity.ok(productService.updateProductById(id, dto));

        Product updatedProduct = productService.updateProductById(id, dto);

        try {

            String jsonPayload = objectMapper.writeValueAsString(updatedProduct);
            kafkaTemplate.send(TOPIC_NAME, String.valueOf(id), jsonPayload);
            System.out.println("Event successfully sent to Kafka for Product Id:" + id);

        } catch (Exception e) {
            System.err.println("Failed to publish product event to Kafka" + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    // FIXED ENDPOINT: Triggers the price topic correctly using the service instance
    @PutMapping("/{id}/price")
    public ResponseEntity<String> updateProductPrice(@PathVariable Long id, @RequestBody String priceJson) {

        // FIXED: Changed from 'ProductService' (Static) to 'productService' (Instance variable)
        productService.sendPriceUpdateEvent(id, priceJson);

        return ResponseEntity.ok("Product price updated and event streamed successfully!");
    }
}
