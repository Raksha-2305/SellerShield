package com.flowpay.flowpaybackend.controller;

import com.flowpay.flowpaybackend.entity.CustomerOrder;
import com.flowpay.flowpaybackend.service.CustomerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import com.flowpay.flowpaybackend.entity.Seller;
import com.flowpay.flowpaybackend.repository.SellerRepository;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/order")
public class CustomerOrderController {

    @Autowired
    private CustomerOrderService customerOrderService;

    @Autowired
    private SellerRepository sellerRepository;

    @PostMapping("/create")
    public CustomerOrder createOrder(@RequestBody CustomerOrder order,Authentication authentication) throws Exception {

        String username = authentication.getName();

        Seller seller =sellerRepository.findByUsername(username);

        if (seller == null) {
            throw new RuntimeException("Logged-in seller not found");
        }

        return customerOrderService.createOrder(order,seller);
    }

    @GetMapping("/all")
    public List<CustomerOrder> getAllOrders(Authentication authentication) {

        String username = authentication.getName();

        Seller seller =sellerRepository.findByUsername(username);

        if (seller == null) {
            throw new RuntimeException("Logged-in seller not found");
        }

        return customerOrderService.getAllOrders(seller);
    }

    @GetMapping("/{id}")
    public CustomerOrder getOrderById(@PathVariable Long id) {
        return customerOrderService.getOrderById(id);
    }

    @PutMapping("/{id}/paid")
    public CustomerOrder markAsPaid(@PathVariable Long id,Authentication authentication) {

        String username = authentication.getName();

        Seller seller =sellerRepository.findByUsername(username);

        if (seller == null) {
            throw new RuntimeException("Logged-in seller not found");
        }

        return customerOrderService.markAsPaid(id,seller);
    }

    @GetMapping("/payment/{paymentToken}")
    public CustomerOrder getOrderByPaymentToken(
        @PathVariable String paymentToken) {

            CustomerOrder order =customerOrderService.getOrderByPaymentToken(paymentToken);

            if (order == null) {
                throw new RuntimeException("Invalid payment token");
            }

        return order;
    }

    @GetMapping("/payment-link/{id}")
    public String getCustomerPaymentUrl(@PathVariable Long id) {
        return customerOrderService.getCustomerPaymentUrl(id);
    }

    @GetMapping("/qr/{fileName}")
    public ResponseEntity<Resource> getQrCode(@PathVariable String fileName) {
        Resource resource = new FileSystemResource("qrcodes/" + fileName);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().header(HttpHeaders.CONTENT_TYPE, "image/png").body(resource);
    }

    @PutMapping("/{id}/regenerate-qr")
    public CustomerOrder regenerateQrCode(@PathVariable Long id) throws Exception {
        return customerOrderService.regenerateQrCode(id);
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id, Authentication authentication) {

        String username = authentication.getName();

        Seller seller = sellerRepository.findByUsername(username);

        if (seller == null) {
            throw new RuntimeException("Logged-in seller not found");
        }

        customerOrderService.deleteOrder(id, seller);
    }
}
