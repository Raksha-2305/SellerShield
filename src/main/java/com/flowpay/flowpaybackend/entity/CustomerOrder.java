package com.flowpay.flowpaybackend.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String customerName;
    private String phoneNumber;
    private String address;
    private Double amount;
    private String selectedUpiId;
    private String paymentStatus;
    private String paymentLink;
    private String qrCode;
    private String paymentToken;
    private String razorpayOrderId;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    private Seller seller;

}
