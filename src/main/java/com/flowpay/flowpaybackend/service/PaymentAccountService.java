package com.flowpay.flowpaybackend.service;

import com.flowpay.flowpaybackend.entity.PaymentAccount;
import com.flowpay.flowpaybackend.entity.Seller;
import com.flowpay.flowpaybackend.repository.PaymentAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentAccountService {

    @Autowired
    private PaymentAccountRepository paymentAccountRepository;

    public PaymentAccount addPaymentAccount(
            PaymentAccount paymentAccount,
            Seller seller) {

        paymentAccount.setSeller(seller);

        return paymentAccountRepository.save(paymentAccount);
    }

    public List<PaymentAccount> getSellerPaymentAccounts(
            Seller seller) {

        return paymentAccountRepository.findBySeller(seller);
    }

    public void deletePaymentAccount(
            Long id,
            Seller seller) {

        PaymentAccount account =
                paymentAccountRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Payment account not found"));

        if (!account.getSeller().getId().equals(seller.getId())) {
            throw new RuntimeException(
                    "You cannot delete another seller's account");
        }

        paymentAccountRepository.delete(account);
    }
}