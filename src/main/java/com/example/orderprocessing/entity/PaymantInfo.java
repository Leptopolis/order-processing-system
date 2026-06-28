package com.example.orderprocessing.entity;

import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;


@Data
@Embedded
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInfo{

    private String paymentId;
    private String paymentMethod;
    private String transactionId;
    private LocalDateTime paidAt;
    private boolean isPaid = false;
}