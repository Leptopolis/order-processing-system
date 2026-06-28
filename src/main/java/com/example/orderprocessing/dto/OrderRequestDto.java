package com.example.orderprocessing.dto;

import lombok.*;
import jakarta.persistence.*;

import java.util.List;

@Data
public class OrderRequestDto{
    @NotNull(message = "Id is important")
    @Positive(message = "Buyers id must be positive")
    private Long customerId;

    @NotEmpty(message = "Order must consist at least at one product")
    @Valid
    private List<OrderItemDto> items;

    @NotNull(message = "Paymant method is musthave")
    private String paymentMethod;
}