package com.flowpay.flowpaybackend.controller;

import com.flowpay.flowpaybackend.entity.CustomerOrder;
import com.flowpay.flowpaybackend.service.CustomerOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.flowpay.flowpaybackend.entity.Seller;
import com.flowpay.flowpaybackend.repository.SellerRepository;
import org.springframework.security.core.Authentication;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.core.io.Resource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;

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

    @GetMapping("/qr/{fileName}")
    public ResponseEntity<Resource> getQrCode(
        @PathVariable String fileName) throws IOException {

    Path qrDirectory = Paths.get("qrcodes")
        .toAbsolutePath()
        .normalize();

    Path imagePath = qrDirectory.resolve(fileName).normalize();

    if (!imagePath.startsWith(qrDirectory)) {
        return ResponseEntity.badRequest().build();
    }

    Resource resource = new FileSystemResource(imagePath);

    if (!resource.exists() || !resource.isReadable()) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok()
        .contentType(MediaType.IMAGE_JPEG)
        .body(resource);    
    }
}
