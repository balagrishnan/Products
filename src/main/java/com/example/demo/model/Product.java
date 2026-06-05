package com.example.demo.model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="Product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    private String description;

    private Double price;
}
