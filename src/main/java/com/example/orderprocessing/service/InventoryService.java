package com.example.orderprocessing.service;

import com.example.orderprocessing.entity.Order;
import com.example.orderprocessing.entity.OrderItem;
import com.example.orderprocessing.entity.Product;
import com.example.orderprocessing.exception.InventoryException;
import com.example.orderprocessing.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService{
    private final ProductRepository;
    private final ReentrantLock stockLock = new ReentrantLock();

    @Transactional
    public void reserveProducts(Order order){
        log.info("Reservation product for order:{}", order.getOrderNumber());

        stockLock.lock();
        try{
            for(OrderItem item : order.getItems()){
                Product product = productRepository.findByIdWithLock(item.getProduct().getId())
                    .orElseThrow(()-> new InventoryException(
                        "Product is not found: " + item.getProduct().getId();
                    ));
                
                if(product.getStockQuantity() < item.getQuantity()){
                    throw new InventoryException(
                        String.format("There is no so much product %s, We have: %d, You need: %d",
                        product.getName(), product.getStockQuantity(), item.getQuantity())
                    );
                }
                product.decreaseStock(item.getQuantity());
                productRepository.save(product);
                log.debug("{} Product {} units reserved". item.getQuantity(), product.getname());
            }
        } finally {
            stockLock.unlock();
        }
    }

    public Product findProductById(Long Id){
        return productRepository.findById(id)
            .orElseThrow(() -> new InventoryException("Product is not found: " + id));
    }
}