package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.repository.OrderRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class PaymentQrController {

    private final OrderRepository orderRepository;

    public PaymentQrController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping("/qr/payment/{orderId}")
    public void generatePaymentQr(@PathVariable Long orderId,
                                  HttpServletResponse response) throws Exception {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        String bankId = "TPB";
        String accountNumber = "07737833901";
        String accountName = "PHAN HOAI TAN";
        String template = "compact2";

        long amount = Math.round(order.getTotalAmount());
        String addInfo = "JTECH" + order.getId();

        String qrUrl = "https://img.vietqr.io/image/"
                + bankId + "-"
                + accountNumber + "-"
                + template + ".png"
                + "?amount=" + amount
                + "&addInfo=" + URLEncoder.encode(addInfo, StandardCharsets.UTF_8)
                + "&accountName=" + URLEncoder.encode(accountName, StandardCharsets.UTF_8);

        response.sendRedirect(qrUrl);
    }
}