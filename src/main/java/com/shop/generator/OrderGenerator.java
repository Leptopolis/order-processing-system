package com.shop.generator;

import com.shop.model.Order;
import com.shop.model.OrderItem;
import com.shop.queue.OrderQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

public class OrderGenerator {
    private final OrderQueue queue;
    private final Random random = new Random();
    private final AtomicLong ordersGenerated = new AtomicLong(0);
    private volatile boolean running = true;

    private static final String[] PRODUCTS = {
        "Laptop", "Phone", "Tablet", "Monitor", "Keyboard",
        "Mouse", "Charger", "Case", "Cable"
    };

    public OrderGenerator(OrderQueue queue){
        this.queue = queue;
    }

    @Override
    public void run(){
        System.out.println("OrderGenerator Started");

        while(running){
            try{
                Order order = generateOrder();
                queue.add(order);
                ordersGenerated.incrementAndGet();
                Thread.sleep(random.nextInt(10) + 1);
            } catch {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println("OrderGenerator Ended");
    }

    private Order generateOrder(){
        int itemCount = random.nextInt(5) + 1;
        List<OrderItem> items = new ArrayList<>();

        for(int i = 0;i < itemCount;i++){
            String productId = "p" + (random.nextInt(100) + 1);
            String productName = PRODUCTS[random.nextInt(PRODUCTS.length)];
            int quantity = random.nextInt(3) + 1;
            double price = 10 + random.nextDouble() * 900;
            items.add(new OrderItem(productId, productName, quantity, Math.round(price * 100) / 100.0));
        }
        Order order = new Order(items);
        order.setCustomerId("C" + (random.nextInt(1000) + 1));
    }

    public void stop(){
        running = false;
    }
    public long getOrdersGenerated(){
        return ordersGenerated;
    }
}