package com.flowpay.flowpaybackend.repository;

import com.flowpay.flowpaybackend.entity.PaymentAccount;
import com.flowpay.flowpaybackend.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentAccountRepository
        extends JpaRepository<PaymentAccount, Long> {

    List<PaymentAccount> findBySeller(Seller seller);

    PaymentAccount findBySellerAndUpiId(
            Seller seller,
            String upiId
    );
}