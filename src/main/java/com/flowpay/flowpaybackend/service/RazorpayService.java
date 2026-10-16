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

    long amountInPaise = Math.round(amount * 100);

    String upiLink = "upi://pay"
            + "?pa=" + java.net.URLEncoder.encode(
                    upiId, StandardCharsets.UTF_8)
            + "&pn=" + java.net.URLEncoder.encode(
                    sellerName, StandardCharsets.UTF_8)
            + "&am=" + String.format(
                    java.util.Locale.US, "%.2f", amount)
            + "&cu=INR"
            + "&tn=" + java.net.URLEncoder.encode(
                    orderDescription, StandardCharsets.UTF_8);

    Path qrFolder = Paths.get("qrcodes");
    Files.createDirectories(qrFolder);

    String fileName = "seller_" + System.currentTimeMillis() + ".jpeg";
    Path qrFile = qrFolder.resolve(fileName);

    com.google.zxing.MultiFormatWriter writer =
            new com.google.zxing.MultiFormatWriter();

    com.google.zxing.common.BitMatrix matrix =
            writer.encode(
                    upiLink,
                    com.google.zxing.BarcodeFormat.QR_CODE,
                    400,
                    400
            );

    com.google.zxing.client.j2se.MatrixToImageWriter.writeToPath(
            matrix,
            "JPEG",
            qrFile
    );

    return "local|" + fileName;
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
