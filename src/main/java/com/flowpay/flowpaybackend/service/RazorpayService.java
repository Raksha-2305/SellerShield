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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    
public String createRazorpayQr(
        Double amount,
        String upiId,
        String sellerName,
        String orderDescription) throws Exception {

    if (amount == null || amount <= 0) {
        throw new IllegalArgumentException("Amount must be greater than zero");
    }

    long amountInPaise = Math.round(amount * 100);

    String json = "{"
            + "\"type\":\"upi_qr\","
            + "\"name\":\"" + escapeJson(sellerName) + "\","
            + "\"usage\":\"single_use\","
            + "\"fixed_amount\":true,"
            + "\"payment_amount\":" + amountInPaise + ","
            + "\"description\":\"" + escapeJson(orderDescription) + "\""
            + "}";

    String responseBody = sendRazorpayRequest(
            "https://api.razorpay.com/v1/payments/qr_codes",
            json
    );

    String qrId = extractValue(responseBody, "id");
    String imageUrl = extractValue(responseBody, "image_url");
    System.out.println("Razorpay QR image URL: " + imageUrl);

    if (qrId == null || imageUrl == null) {
        throw new RuntimeException(
                "Razorpay QR response missing id or image_url: "
                        + responseBody
        );
    }

    // Download the QR image for the existing app's QR display flow.
    Path qrFolder = Paths.get("qrcodes");
    Files.createDirectories(qrFolder);

    String fileName = "razorpay_" + qrId + ".png";
    Path qrFile = qrFolder.resolve(fileName).normalize();

    HttpRequest imageRequest = HttpRequest.newBuilder()
            .uri(URI.create(imageUrl))
            .GET()
            .build();

    HttpResponse<byte[]> imageResponse =
            HttpClient.newHttpClient().send(
                    imageRequest,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

    if (imageResponse.statusCode() < 200
            || imageResponse.statusCode() >= 300) {
        throw new RuntimeException(
                "Unable to download Razorpay QR image"
        );
    }

        Files.write(qrFile, imageResponse.body());

    // Format: local|filename|razorpayQrCodeId
        return "local|" + fileName + "|" + qrId;
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

        return json.substring(start, end).replace("\\/", "/");
    }

    public String extractQrId(String responseBody) {
        return extractValue(responseBody, "id");
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
