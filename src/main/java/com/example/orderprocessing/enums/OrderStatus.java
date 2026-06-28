package com.example.orderprocessing.enums;


public enum OrderStatus {
    CREATED,      // Заказ создан
    PROCESSING,   // В обработке
    PAID,         // Оплачен
    SHIPPED,      // Отправлен
    DELIVERED,    // Доставлен
    CANCELLED     // Отменен
}