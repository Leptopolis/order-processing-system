package com.shop.queue;

import com.shop.model.Order;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

public class OrderQueue {
    private final BlockingQueue<Order> queue;
    private final AtomicLong totalAdded = new AtomicLong(0);
    private final AtomicLong totalTaken = new AtomicLong(0);
    private final int maxSize;

    public void OrderQueue(int maxSize){
        this.maxSize = maxSize;
        this.queue = new LinkedBlockingQueue<>(maxSize);
    }

    public void add(Order order) throws InterruptedException {
        queue.put(order);
        totaalAdded.incrementAndGet();
    }

    public Order take() throws InterruptedException {
        Order order = queue.take();
        totalTaken.incrementAndGet();
        return order;
    }

    public Order poll(){
        Order order = queue.poll();
        if(order != null){
            totalTaken.incrementAndGet();
        }
        return order;
    }

    public int size(){
        return queue.size();
    }

    public long getTotalAdded(){
        return totalAdded;
    }

    public long getTotalTaken(){
        return totalTaken;
    }

    public int getMaxSize(){
        return maxSize;
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }

}