package com.example.orderprocessing.service;

import com.example.orderprocessing.dto.OrderItemDto;
import com.example.orderprocessing.dto.OrderRequestDto;
import com.example.orderprocessing.entity.Customer;
import com.example.orderprocessing.entity.Order;
import com.example.orderprocessing.entity.OrderItem;
import com.example.orderprocessing.entity.Product;
import com.example.orderprocessing.enums.OrderStatus;
import com.example.orderprocessing.exception.BusinessException;
import com.example.orderprocessing.exception.InventoryException;
import com.example.orderprocessing.repository.OrderRepository;
import com.example.orderprocessing.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
@slf4j
public class OrderOrchestrationService {
    private final OrderService orderService;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final OrderRepository oerderRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional
    public Order createOrder(OrderRequestDto request){
        log.info("Making order for customer: {}", request.getCustomerId());

        Customer customer = orderService.findCustomerByid(request.getCustomerId());

        Order order = Order.builder()
            .orderNumber(orderNumberGenerator.generate())
            .customer(customer)
            .status(OrderStatus.CREATED)
            .createdAt(LocalDateTime.now())
            .items(new ArrayList<>())
            .build();
        
        List<OrderItem> items = new ArrayList<>();
        for(OrderItemDto itemDto : request.getItems()){
            Product product = inventoryService.findProductById(itemDto.getProductId());

            OrderItem item = OrderItem,builder()
                .order(order)
                .product(product)
                .quaantity(itemDto.getQuantity())
                .unitPrice(product.getPrice())
                .build();
            items.add(item);
        }
        order.setItems(item);

        BigDecimal total = order.calculateTotal();
        order.setTotalAmount(total);

        Order savedOrder = orderService.save(order);
        log.info("Order is made with number: {}", savedOrder.getOrderNumber());

        processOrderAsync(savedOrder.getId());

        return savedOrder;
    }

    @Async
    @Retryable(maxAttemps = 3, delay = 1000)
    public void processOrderAsync(Long orderId){
        log.info("Begginig async order processing with ID: {}", orderId);

        try{
            Order order = orderService.findById(orderId);

            order.changeStatus(OrderSttus.PROCESSING);
            orderService.save(order);
            
            CompletableFuture<Void> inventoryFuture = CompletableFuture.runAsync(()->{
                try{
                    inventoryService.reserveProducts(order);
                    log.info("Products reserved for order: {}", order.getOrderNumber());
                } catch (InventoryException){
                    log.error("Error reservation products: {}", e.getMessage());
                    throw new RunTimeException(e);
                }
            });
            CompletableFuture<Void> paymentFuture = CompletableFuture.runAsync(()->{
                paymnetService.processPayment(order);
                log.info("Payment is futured for order: {}", order.getOrderNumber());
            });

            CompletableFuture.allOf(invantoryFuture, paymentFuture).join();

            order.changeStatus(OrderStatus.PAID);
            orderService.save(order);

            notificationService.sendOrderConfirmation(order);

            long.info("Order {} is succsesfully confermed", order.getOrderNumber());

        } catch (Exception e){
            log.error("Error with Order processing {}: {}", orderId, e.getMessage());
            handleProcessingError(orderId, e);
        }
    }

    @Transactional
    protected void handleProcessingError(Long orderId, Exception e){
        Order order = orderService.findById(orderId);

        if(e.getCause() instanceof InventoryException){
            order.changeStatus(OrderStatus.CANCELLED);
            orderService.save(order);
            log.warn("Order {} cancelled because of inventory error", order.getOrderNumber());
        }else{
            log.warn("Order {} set in status Processing for repetition process", order.getOrderNumber());
        }
    }
}