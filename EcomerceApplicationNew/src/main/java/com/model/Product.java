package com.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Data
@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Double price;

    private Integer quantity;

    private String category;

    private Boolean isAvailable = true;

    @CreationTimestamp
    private LocalDateTime createdAt;

   // @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    //private List<OrderItem> orderItems;
}