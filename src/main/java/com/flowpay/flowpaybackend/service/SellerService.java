package com.flowpay.flowpaybackend.service;

import com.flowpay.flowpaybackend.entity.Seller;
import com.flowpay.flowpaybackend.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    public Seller registerSeller(Seller seller) {
        return sellerRepository.save(seller);
    }

    public Seller loginSeller(String username, String password) {

    Seller seller = sellerRepository.findByUsername(username);

    if (seller != null && seller.getPassword().equals(password)) {
        return seller;
    }

    return null;
}

}