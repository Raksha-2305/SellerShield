package com.flowpay.flowpaybackend.util;

public class UpiLinkGenerator {

    public static String generateUpiLink(String upiId,String merchantName,Double amount) {

        return "upi://pay?pa="+ upiId+ "&pn="+ merchantName+ "&am="+ amount+ "&cu=INR";
    }
}