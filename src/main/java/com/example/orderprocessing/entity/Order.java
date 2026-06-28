package com.example.orderprocessing.entity;

import com.example.orderprocessing.enums.OrderStatus;
import jakarata.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order extends BaseEntity{

    @Column(unique = true, nullable = false, length = 50)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.Created;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;
    
    @Column(name = "created_at")
    private LocalDateTime creaatedAt;

    @ManyToOne
    @Column(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    @Embedded
    private PaymentInfo paymentInfo;

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Order order = (Order)o;
        return Objects.equals(orderNumber, order.orderNumber);
    }

    @Override
    public int hashCode(){
        return Objects.hashCode(orderNumber);  
    }

    public BigDecimal calculateTotal(){
        return items.stream()
                    .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void changeStatus(OrderStatus newStatus){
        if(this.status == OrderStatus.CANCELLED || this.status == OrderStatus.DELIVERED){
            throw new IllegalStateException(
                String.format("Cant change status %s, it has status: %s", 
                    this.orderNumber, this.status)
            );
        }
        this.status = newStatus;
    }
}   