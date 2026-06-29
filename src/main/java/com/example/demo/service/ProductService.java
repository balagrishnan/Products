package com.example.demo.service;

import org.springframework.stereotype.Service;
import com.example.demo.repository.ProductRepository;
import com.example.demo.model.Product;
import com.example.demo.dto.ProductDTO;
import java.util.List;
import lombok.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Service
@RequiredArgsConstructor // Automatically creates the constructor for BOTH productRepository and kafkaTemplate
public class ProductService {

    // Marking both dependencies as 'final' lets Lombok cleanly inject them via constructor injection
    private final ProductRepository productRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);
    private static final String PRICE_TOPIC = "product-price-updates";

    public List<Product> getAllProducts(){
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id " + id));
    }

    public Product createProduct(ProductDTO dto) {
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .quantity(dto.getQuantity())
                .build();
        return productRepository.save(product);
    }

    public Product updateProductById(Long id, ProductDTO dto) {
        Product product = getProductById(id);

        // Track the old price to verify if a change actually occurred
        double oldPrice = product.getPrice();

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(dto.getCategory());
        product.setQuantity(dto.getQuantity());

        Product updatedProduct = productRepository.save(product);

        // AUTOMATION: If the price changed, automatically fire the event!
        if (oldPrice != dto.getPrice()) {
            String priceJson = String.format("{\"productId\": %d, \"oldPrice\": %.2f, \"newPrice\": %.2f}",
                    id, oldPrice, dto.getPrice());
            sendPriceUpdateEvent(id, priceJson);
        }

        return updatedProduct;
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public void sendPriceUpdateEvent(Long productId, String priceJson) {
        logger.info("Publishing price update event to Kafka for Product ID: {}", productId);

        kafkaTemplate.send(PRICE_TOPIC, String.valueOf(productId), priceJson)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        logger.info("Successfully sent price update to partition: {}",
                                result.getRecordMetadata().partition());
                    } else {
                        logger.error("Failed to publish price update to Kafka", ex);
                    }
                });
    }
}