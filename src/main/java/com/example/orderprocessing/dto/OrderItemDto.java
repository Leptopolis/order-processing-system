package com.example.orderprocessing.dto;

import lombok.*;
import jakarta.persistence.*;

@Data
public class OrderItemDto{

    @NotNull(message = "Product id is must have")
    @Positive(message = "Product id must be positive")
    private Long productId;

    @NotNull(message = "Quantity is must have")
    @Positive(message = "Quatity must be positive")
    private Integer quantity;
}