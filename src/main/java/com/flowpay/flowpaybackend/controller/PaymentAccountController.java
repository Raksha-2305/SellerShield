package com.flowpay.flowpaybackend.controller;

import com.flowpay.flowpaybackend.entity.PaymentAccount;
import com.flowpay.flowpaybackend.entity.Seller;
import com.flowpay.flowpaybackend.repository.SellerRepository;
import com.flowpay.flowpaybackend.service.PaymentAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payment-accounts")
public class PaymentAccountController {

    @Autowired
    private PaymentAccountService paymentAccountService;

    @Autowired
    private SellerRepository sellerRepository;

    @PostMapping
    public PaymentAccount addPaymentAccount(
            @RequestBody PaymentAccount paymentAccount,
            Authentication authentication) {

        Seller seller = getLoggedInSeller(authentication);

        return paymentAccountService.addPaymentAccount(
                paymentAccount,
                seller
        );
    }

    @GetMapping
    public List<PaymentAccount> getPaymentAccounts(
            Authentication authentication) {

        Seller seller = getLoggedInSeller(authentication);

        return paymentAccountService.getSellerPaymentAccounts(
                seller
        );
    }

    @DeleteMapping("/{id}")
    public void deletePaymentAccount(
            @PathVariable Long id,
            Authentication authentication) {

        Seller seller = getLoggedInSeller(authentication);

        paymentAccountService.deletePaymentAccount(
                id,
                seller
        );
    }

    private Seller getLoggedInSeller(
            Authentication authentication) {

        String username = authentication.getName();

        Seller seller =
                sellerRepository.findByUsername(username);

        if (seller == null) {
            throw new RuntimeException(
                    "Logged-in seller not found"
            );
        }

        return seller;
    }
}