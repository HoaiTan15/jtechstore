package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.model.OrderDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendResetPasswordCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận đặt lại mật khẩu - J-Tech Store");

        message.setText("""
                Xin chào,

                Bạn đang yêu cầu đặt lại mật khẩu tại J-Tech Store.

                Mã xác nhận của bạn là: %s

                Mã này có hiệu lực trong 5 phút.

                Nếu bạn không yêu cầu, vui lòng bỏ qua email này.

                J-Tech Store
                """.formatted(code));

        mailSender.send(message);
    }

    public void sendPaymentBill(String toEmail, Order order) {
        StringBuilder content = new StringBuilder();

        content.append("Xin chào ").append(order.getCustomerName()).append(",\n\n");
        content.append("J-Tech Store xác nhận đơn hàng của bạn đã thanh toán thành công.\n\n");

        content.append("Mã đơn hàng: ").append(order.getId()).append("\n");
        content.append("Số điện thoại: ").append(order.getPhone()).append("\n");
        content.append("Địa chỉ giao hàng: ").append(order.getAddress()).append("\n");
        content.append("Trạng thái đơn hàng: ").append(order.getStatus()).append("\n");
        content.append("Trạng thái thanh toán: ").append(order.getPaymentStatus()).append("\n\n");

        content.append("Chi tiết sản phẩm:\n");

        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                double lineTotal = detail.getPrice() * detail.getQuantity();

                content.append("- ")
                        .append(detail.getProduct() != null ? detail.getProduct().getName() : "Sản phẩm")
                        .append(" | SL: ")
                        .append(detail.getQuantity())
                        .append(" | Giá: ")
                        .append(String.format("%,.0f VNĐ", detail.getPrice()))
                        .append(" | Thành tiền: ")
                        .append(String.format("%,.0f VNĐ", lineTotal))
                        .append("\n");
            }
        }

        double subtotal = order.getSubtotalAmount() != null ? order.getSubtotalAmount() : order.getTotalAmount();
        double discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : 0;

        content.append("\nTạm tính: ")
                .append(String.format("%,.0f VNĐ", subtotal))
                .append("\n");

        if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
            content.append("Mã giảm giá: ")
                    .append(order.getCouponCode())
                    .append("\n");
        }

        if (discount > 0) {
            content.append("Số tiền giảm: -")
                    .append(String.format("%,.0f VNĐ", discount))
                    .append("\n");
        }

        content.append("Tổng thanh toán: ")
                .append(String.format("%,.0f VNĐ", order.getTotalAmount()))
                .append("\n\n");

        content.append("Cảm ơn bạn đã mua hàng tại J-Tech Store.");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Hóa đơn thanh toán đơn hàng #" + order.getId() + " - J-Tech Store");
        message.setText(content.toString());

        mailSender.send(message);
    }
}