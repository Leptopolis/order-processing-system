package com.shop.model;

import java.util.List;
import java.util.UUID;

public class Order{
    private final String id;
    private final List<OrderItem> items;
    private OrderStatus status;
    private double totalAmount;
    private String customerId;
    private long createdAt;
    private long processedAt;

    public Order(List<OrderItem> items){
        this.id = UUID.randomUUID().toString();
        this.items = items;
        this.status = OrderStatus.CREATED;
        this.createdAt = System.currentTimeMillis();
    }

    //getters
    public String getId() { return id; }
    public List<OrderItem> getItems() { return items; }
    public OrderStatus getStatus() { return status; }
    public String getCutomerID() { return customerId; }
    public double getTotalAmount() { return totalAmount; }
    public long getCreatedAt() { return createdAt; }
    
    //setters
    public void setStatus(OrderStatus status) { this.status = status; }
    public void setTotalAmount(long totalAmount) { this.totalAmount = totalAmount; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public void setProcessedAt(long processedAt) { this.processedAt = processedAt; }

}