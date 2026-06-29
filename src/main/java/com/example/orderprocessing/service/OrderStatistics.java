package com.example.orderprocessing.service;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OrderStatistics {
    private String customerName;
    private long totalOrders;
    private long completedOrders;
    private long activeOrders;
    private long cancelledOrders;

    public double getCompletionRate() {
        if (totalOrders == 0) return 0.0;
        return (double) completedOrders / totalOrders * 100;
    }
}