package com.example.orderprocessing.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @Column(length = 50)
    private String category;

    public synchronized void decreaseStock(int quantity){
        if(this.stockQuantity < quantity){
            throw new IllegalArgumentException{
                String.format("There is no product %s, We have %d, You want %d",
                            this.name, quantity, this.stockQuantity);
            };
        }
        this.stockQuantity -= quantity;
    }

    public synchronized void IncreaseStock(int quantity){
        this.stockQuantity += quantity;
    }
}