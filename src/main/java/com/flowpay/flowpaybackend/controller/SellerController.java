package com.flowpay.flowpaybackend.controller;

import com.flowpay.flowpaybackend.entity.Seller;
import com.flowpay.flowpaybackend.service.SellerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.flowpay.flowpaybackend.dto.LoginRequest;

@RestController
@RequestMapping("/seller")
public class SellerController {

    @Autowired
    private SellerService sellerService;

    @PostMapping("/register")
    public Seller registerSeller(@RequestBody Seller seller) {
        return sellerService.registerSeller(seller);
    }

    @PostMapping("/login")
    public Seller loginSeller(@RequestBody LoginRequest loginRequest) {

    return sellerService.loginSeller(
            loginRequest.getUsername(),
            loginRequest.getPassword()
    );
}
}