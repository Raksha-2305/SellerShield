package com.flowpay.flowpaybackend.service;

import com.flowpay.flowpaybackend.entity.Seller;
import com.flowpay.flowpaybackend.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SellerUserDetailsService implements UserDetailsService {

    @Autowired
    private SellerRepository sellerRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        Seller seller = sellerRepository.findByUsername(username);

        if (seller == null) {
            throw new UsernameNotFoundException(
                "Seller not found: " + username
            );
        }

        return User.withUsername(seller.getUsername())
                .password(seller.getPassword())
                .roles("SELLER")
                .build();
    }
}