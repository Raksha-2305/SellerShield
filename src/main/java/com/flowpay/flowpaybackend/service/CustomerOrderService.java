package com.flowpay.flowpaybackend.service;

import com.flowpay.flowpaybackend.entity.CustomerOrder;
import com.flowpay.flowpaybackend.entity.PaymentAccount;
import com.flowpay.flowpaybackend.entity.Seller;
import com.flowpay.flowpaybackend.repository.CustomerOrderRepository;
import com.flowpay.flowpaybackend.repository.PaymentAccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CustomerOrderService {

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    @Autowired
    private RazorpayService razorpayService;


    public CustomerOrder createOrder(
            CustomerOrder order,
            Seller seller) throws Exception {

        order.setSeller(seller);

        order.setPaymentStatus("PENDING");

        String paymentToken = UUID.randomUUID().toString();
        order.setPaymentToken(paymentToken);

        order.setCreatedAt(LocalDateTime.now());


        // Check selected seller payment account
        PaymentAccount paymentAccount =
                paymentAccountRepository.findBySellerAndUpiId(
                        seller,
                        order.getSelectedUpiId()
                );

        if (paymentAccount == null) {
            throw new RuntimeException(
                    "Invalid payment account selected"
            );
        }


        // Create Razorpay UPI QR
        String qrImageUrl =
                razorpayService.createRazorpayQr(
                        order.getAmount(),
                        "FlowPay Order " + paymentToken
                );


        // Save Razorpay QR URL
        order.setQrCode(qrImageUrl);


        return customerOrderRepository.save(order);
    }


    public List<CustomerOrder> getAllOrders(Seller seller) {

        return customerOrderRepository.findBySeller(seller);
    }


    public CustomerOrder getOrderById(Long id) {

        return customerOrderRepository
                .findById(id)
                .orElse(null);
    }


    public CustomerOrder getOrderByPaymentToken(
            String paymentToken) {

        return customerOrderRepository
                .findByPaymentToken(paymentToken)
                .orElse(null);
    }


    public CustomerOrder markAsPaid(
            Long id,
            Seller seller) {

        CustomerOrder order =
                customerOrderRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order not found"
                                )
                        );


        if (!order.getSeller().getId().equals(seller.getId())) {

            throw new RuntimeException(
                    "You cannot update another seller's order"
            );
        }


        if ("PAID".equals(order.getPaymentStatus())) {

            throw new RuntimeException(
                    "Order is already marked as PAID"
            );
        }


        order.setPaymentStatus("PAID");

        order.setPaidAt(LocalDateTime.now());


        return customerOrderRepository.save(order);
    }


    public CustomerOrder regenerateQrCode(Long id)
            throws Exception {

        CustomerOrder order =
                customerOrderRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order not found"
                                )
                        );


        String qrImageUrl =
                razorpayService.createRazorpayQr(
                        order.getAmount(),
                        "FlowPay Order " +
                                order.getPaymentToken()
                );


        order.setQrCode(qrImageUrl);


        return customerOrderRepository.save(order);
    }


    public String getCustomerPaymentUrl(Long id) {

        CustomerOrder order =
                customerOrderRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new RuntimeException(
                                        "Order not found"
                                )
                        );


        return "http://localhost:8080/?token="
                + order.getPaymentToken();
    }

    public void deleteOrder(Long id, Seller seller) {

        CustomerOrder order = customerOrderRepository.findById(id).orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException("You cannot delete another seller's order");
        }

        customerOrderRepository.delete(order);
    }
}
