package com.example.orderprocessing.service;

import com.example.orderprocessing.entity.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService{
    private final JavaMailSender mailSender;

    @Value("${app.notification.from-email:noreply@order-system.com}")
    private String fromEmail;


    @Async
    public void sendOrderConfirmation(Order order){
        log.info("Sending confirmation for order: {}", order.getOrderNumber());

        try{
            String subject = "Order confirmation #" + order.getOrderNumber();
            String content = buildOrderConfirmationContent(order);

            sendEmail(order.getCustomer().getEmail(), subject, content);

            log.info("Order confirmation is send fo.r order: {}", order.getOrderNumber());
        } catch (Exception e){
            log.error("Error in sending order confirmation for order: {}", e.getMessage());
        }
    }

    @Async
    public void sendPaymentConfirmtion(Order order){
        log.info("Sending payment confirmation for order: {}", order.getOrderNumber());

        try{
            String subject = "Payment confirmation for order #" + order.getOrderNumber();
            String content = buildPaymentConfirmationContent(order);

            sendEmail(order.getCustomer().getEmail(), subject, content);

            log.info("Payment confirmation is send for order: {}", order.getOrderNumber());
        } catch (Exception e){
            log.error("Error in sending payment confirmation for order: {}", e.getMessage());
        }
    }

    @Async 
    void sendShipmentNotification(Order order){
        log.info("Sending notification of shipment for order: {}", order.getOrderNumber());

        try{
            String subject = "Order#" + order.getOrderNumber() + " shiped";
            String content = "You'r order is send.";

            sendEmail(Order.getCustomer().getEmail(), subject, content);

            log.info("Notification of shipment of send for order: {}", order.getOrderNumber());
        } catch (Exception e){
            log.error("Error in sending shipment notification: {}", e.getMessage());
        }
    }

    @Async
    public void sendRefundConfirmation(Order order){
        log.info("Sending refun confirmation for order: {}", order.getOrderNumber());

        try{
            String subject = "Refund money for order #" + order.getOrderNumber();
            String content = "Money for order#" + order.getOrderNumber()  + " in amount " + order.getTotalAmount() + " is refuned";

            sendEmail(order.getCustomer().getEmail(), subject, content);

            log.info("Refund confirmation is send on :{}", order.getCustomer().getEmail());
        } catch (Exception e){
            log.error("Error in refund confirmation: {}", e.getMessage());
        }
    }

    @Async
    public void sendOrderCancellation(Order order){
        log.info("Sending notificaation about order cancellation for order: {}", order.getOrderNumber());

        try{
            String subject = "Cancelling order#" + order.getOrderNumber();
            String content = "Your order#" + order.getOrderNumber() + " is cancelled";

            sendEmail(order.getCustomer().getEmail(), subject, content);

            log.info("Notification about order cancellation is send to: {}", order.getCustomer().getEmail());
        } catch (Exception e){
            log.error("Error in sending notification about order cancellation: {}", e.getMessage());
        }
    }

    @Async
    public void sendDelayNotification(Order order, String reason){
        log.info("Sending delay notification for order: {}", order.getOrderNumber());

        try{
            String subject = "Delay order#" + order.getOrderNumber();
            String content = "Order #" + order.getOrderNumber() + " is delayed. Reason: " + reason;

            sendEmail(order.getCustomer().getEmail(), subject, content);

            log.info("Notification about order delay is sent to: {}", order.getCustomer().getEmail());
        } catch (Exception e){
            log.error("Error in sending notification about order delay: {}", e.getMessage());
        }
    }

    private void sendEmail(String to, String subject, String content){
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setContent(content);
            mailSender.send(message);

            log.debug("Email is sent to {}", to);
        } catch (Exception e){
            log.error("error in sending email to {}:{}", to, e.getMessage());
            throw e;
        }
    }

    private String buildOrderConfirmationContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("Hello, ").append(order.getCustomer().getName()).append("!\n\n");
        content.append("Your order #").append(order.getOrderNumber()).append(" is successfully msden.\n");
        content.append("Total amount: ").append(order.getTotalAmount()).append("\n");
        content.append("Status: ").append(order.getStatus()).append("\n\n");

        content.append("Order:\n");
        order.getItems().forEach(item -> {
            content.append("- ").append(item.getProduct().getName())
                .append(" x ").append(item.getQuantity())
                .append(" = ").append(item.getSubtotal()).append("\n");
        });

        content.append("\nThanks for buying!");
        return content.toString();
    }

    private String buildPaymentConfirmationContent(Order order) {
        StringBuilder content = new StringBuilder();
        content.append("Hello, ").append(order.getCustomer().getName()).append("!\n\n");
        content.append("Order payment#").append(order.getOrderNumber()).append(" successfully confermed.\n");
        content.append("Total amount: ").append(order.getTotalAmount()).append("\n");
        content.append("Date: ").append(order.getPaymentInfo().getPaidAt()).append("\n\n");

        if (order.getPaymentInfo().getTransactionId() != null) {
            content.append("Transaction Id: ").append(order.getPaymentInfo().getTransactionId()).append("\n");
        }

        content.append("\nYour order is ready for transporttion.");
        return content.toString();
    }

}