package com.flowpay.flowpaybackend.service;

import com.flowpay.flowpaybackend.config.RazorpayConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class RazorpayService {

    @Autowired
    private RazorpayConfig razorpayConfig;

    public String getKeyId() {
        return razorpayConfig.getKeyId();
    }

    // ============================================================
    // CREATE RAZORPAY ORDER
    // ============================================================

    public String createRazorpayOrder(Double amount) throws Exception {

        long amountInPaise = Math.round(amount * 100);

        String json = "{"
                + "\"amount\":" + amountInPaise + ","
                + "\"currency\":\"INR\","
                + "\"receipt\":\"receipt_" + System.currentTimeMillis() + "\""
                + "}";

        String responseBody = sendRazorpayRequest(
                "https://api.razorpay.com/v1/orders",
                json
        );

        return extractValue(responseBody, "id");
    }


    // ============================================================
    // CREATE RAZORPAY UPI QR
    // ============================================================

    public String createRazorpayQr(Double amount, String orderDescription)
            throws Exception {

        long amountInPaise = Math.round(amount * 100);

        String json = "{"
                + "\"type\":\"upi_qr\","
                + "\"name\":\"SellerShield Order\","
                + "\"usage\":\"single_use\","
                + "\"fixed_amount\":true,"
                + "\"payment_amount\":" + amountInPaise + ","
                + "\"description\":\"" + escapeJson(orderDescription) + "\""
                + "}";

        String responseBody = sendRazorpayRequest(
                "https://api.razorpay.com/v1/payments/qr_codes",
                json
        );

        System.out.println("Razorpay QR Response:");
        System.out.println(responseBody);

        String imageUrl = extractValue(responseBody, "image_url");

        if (imageUrl != null) {
                imageUrl = imageUrl.replace("\\/", "/");
        }

        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new RuntimeException(
                    "Razorpay QR was created but image_url was not returned."
            );
        }

        return imageUrl;
    }


    // ============================================================
    // COMMON RAZORPAY API REQUEST
    // ============================================================

    private String sendRazorpayRequest(
            String url,
            String json
    ) throws Exception {

        String credentials =
                razorpayConfig.getKeyId()
                        + ":"
                        + razorpayConfig.getKeySecret();

        String encodedCredentials =
                Base64.getEncoder()
                        .encodeToString(
                                credentials.getBytes(StandardCharsets.UTF_8)
                        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header(
                        "Authorization",
                        "Basic " + encodedCredentials
                )
                .header(
                        "Content-Type",
                        "application/json"
                )
                .POST(
                        HttpRequest.BodyPublishers.ofString(json)
                )
                .build();

        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response =
                client.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        System.out.println(
                "Razorpay HTTP Status: "
                        + response.statusCode()
        );

        if (response.statusCode() < 200 ||
                response.statusCode() >= 300) {

            throw new RuntimeException(
                    "Razorpay API failed: "
                            + response.body()
            );
        }

        return response.body();
    }


    // ============================================================
    // SIMPLE JSON VALUE EXTRACTOR
    // ============================================================

    private String extractValue(
            String json,
            String key
    ) {

        String search = "\"" + key + "\":\"";

        int start = json.indexOf(search);

        if (start == -1) {
            return null;
        }

        start = start + search.length();

        int end = json.indexOf("\"", start);

        if (end == -1) {
            return null;
        }

        return json.substring(start, end);
    }


    // ============================================================
    // ESCAPE JSON TEXT
    // ============================================================

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}