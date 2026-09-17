package com.flowpay.flowpaybackend.repository;

import com.flowpay.flowpaybackend.entity.CustomerOrder;
import com.flowpay.flowpaybackend.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository
    extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByPaymentToken(String paymentToken);

    List<CustomerOrder> findBySeller(Seller seller);
}