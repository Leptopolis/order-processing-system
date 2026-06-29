package com.example.orderprocessing.service;

import com.example.orderprocessing.entity.Order;
import com.example.orderprocessing.entity.PaymentInfo;
import com.example.orderprocessing.enums.OrderStatus;
import com.example.orderprocessing.enums.PaymentStatus;
import com.example.orderprocessing.event.OrderEvent;
import com.example.orderprocessing.exception.PaymentException;
import com.example.orderprocessing.repository.OrderRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService{
    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Value("${app.paymenttimeout:30000}")
    private Long paymentTimeOut;

    private static final String PAYMENT_TOPIC = "payment-events";
    private static final String REFUND_TOPIC = "refund-events";

    @Transactional
    public PaymentInfo processPayment(Order order){
        log.info("Begin of payment process for order: {}", order.getOrderNumber());

        try{
            validateOrderBeforePayment(order);

            PaymentInfo paymentInfo = createPaymentInfo(order);
            boolean paymentSuccess = callExternalPaymentSystem(order, paymentInfo);

            if(paymentSuccess){
                paymentInfo.setIsPaid(true);
                paymentInfo.setPaidAt(LocalDateTime.now());
                order.setPaymentInfo(paymentInfo);

                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);

                sendPaymentSuccessEvent(order);

                notificationService.sendPaymentConfirmtion(order);

                log.info("Payment is successfully confered for order: {}", order.getOrderNumber());
                return paymentInfo;
            }else{
                throw PaymentException("Error confirmation payment for order: {}" + order.getOrderNumber());
            }
        } catch (Exception e){
            log.error("Error in async porcess payment: {}", e.getMessage());
            throw new PaymentException("Async payment procces error" + e.getMessage(), e);
        }
    }
    
    @Async
    @Retryable(
        value = {PaymentException.class},
        maxAttemps = 3,
        backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public CompletableFuture<PaymentInfo> processPaymentAsync(Order order){
        log.info("Async payment process for order: {}", order.getOrderNumber());

        return CompletableFuture.supplyAsync(()->{
            try {
                Thread.sleep(1000);
                return processPayment(order);
            } catch (Exception e){
                log.error("Error in async payment process: {}", e.getMessage());
                throw new PaymentException("Erro in async payment process", e);
            }
        });
    }

    @Transactional
    public void refundPayment(Order order){
        log.info("Canceling payment for order: {}", order.getOrderNumber());

        try{
            if(order.getPaymentInfo() == null || !order.getPaymentInfo().getIsPaid()){
                throw new PaymentException("can't cancel payment: payment hasn't been done");
            }

            boolean refundSucces = callExternalPaymentSystem(order);

            if(refundSucces){
                order.getPaymentInfo().setIsPaid(false);
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);

                sendRefundEvent(order);
                notificationService.sendRefundConfirmation(order);
                log.info("Refund is done for order: {}", order.getOrderNumber());
            }else{
                throw new PaymentException("can't do a refund for order: {}" + order.getOrderNumber());
            }
        } catch (Exception e){
            log.error("Error in refund payment: {}", e.getMessage());
            throw new PaymentException("Error in payment refund", e);
        }
    }

    public PaymentStatus checkPaymentStatus(String PaymentId){
        log.info("Checking payment status: {}", paymentId);

        try{
            Thread.sleep(500);
            PaymentStatus[] statuses = PaymentStatus.values();
            int index = (int)(Math.random() * statuses.length);
            return statuses[index];
        } catch ( InterruptedException e){
            Thread.currentThread().interrupt();
            throw new PaymentException("Error checking payment status", e);
        }
    }

    public void validateOrderBeforePayment(Order order){
        if(order == null){
            throw new PaymentException("Order is not found");
        }
        
        if(order.getTotalAmount() == null || order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0){
            throw new PaymentException("Incorrect total sum of order: " + order.getTotalAmount());
        }

        if(order.getStatus() != OrderStatus.CREATED && order.getStatus() != OrderStatus.PROCESSING){
            throw new PaymentException("Can't pay an order in status: " + order.getStatus());
        }

        if(order.getItems() == null || order.getItems().isEmpty()){
            throw new PaymentException("There is no products in the order");
        }
    }

    private PaymentInfo createPaymentInfo(Order order){
        return PaymentInfo.builder()
            .paymentId(UUID.randomUUID().toString());
            .paymentMethod("CARD")
            .transactionId(UUID.randomUUID().toString())
            .paidAt(LocalDateTime.now())
            .isPaid(false)
            .build();
    }

    private boolean callExternalPaymentSystem(Order order, PaymentInfo paymentInfo){
        log.info("Calling external payment system for order: {}", order.getOrderNumber());

        try{
            Thread.sleep(1500);

            if(Math.random() < 0.05){
                log.warn("random error with calling external payment system");
                return false;
            }

            log.info("Payment info: sum={}, method={}, transaction={}",
                order.getTotalAmount(),
                paymentInfo.getPaymentInfo(),
                paymentInfo.getTransactionId());
            return true;
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            return false;
        }
    }
    private boolean callExternalRefundSystem(Order order) {
        log.info("Вызов системы возврата для заказа: {}", order.getOrderNumber());
        try {
            Thread.sleep(1000);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Async
    public void sendPaymentSuccessEvent(Order order){
        try {
            OrderEvent event = OrderEvent.builder()
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .eventType("PAYMENT_SUCCESS")
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount)
                .timestamp(LocalDateTime.now())
                .customerEmail(order.getCustomer().getEmail())
                .build();

            String message = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String> future = 
                kafkaTemplate.send(PAYMENT_TOPIC, order.getOrderNumber(), message);
        }
    }
 
}