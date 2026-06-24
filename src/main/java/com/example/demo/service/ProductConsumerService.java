package com.example.demo.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductConsumerService {
    // Spring constantly monitors the topic in a separate background thread
    @KafkaListener(topics = "product_updates", groupId = "product_processing_group")
    public void consumeProductEvent(String jsonMessage) {

        System.out.println("\n=================================================");
        System.out.println("🚨 KAFKA CONSUMER ALIVE: Received a new product event!");
        System.out.println("Data Payload: " + jsonMessage);
        System.out.println("=================================================");

        // This is where you put your downstream business logic, for example:
        // 1. Send an inventory notification email
        // 2. Sync to a third-party warehouse API
        // 3. Update an analytics dashboard system
    }
}
