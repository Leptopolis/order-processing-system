package com.shop.model;

public class OrderItem{
    private final String productId;
    private final String productName;
    private final int quantity;
    private final double price;

    public OrderItem(String productId, String productName, int quantity, double price){
        this.productId   = productId;
        this.productName = productName;
        this.quantity    = quantity;
        this.price       = price; 
    }

    //getters
    public String getProductId() { return productId; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public double getTotal() { return price * quantity; }

    
}