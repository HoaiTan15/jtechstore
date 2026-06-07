package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.model.OrderDetail;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.repository.OrderRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import com.jtech.jtechstore.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    public AdminOrderController(OrderRepository orderRepository,
                                ProductRepository productRepository,
                                EmailService emailService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("orders", orderRepository.findAllByOrderByOrderDateDesc());
        return "admin/orders/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        model.addAttribute("order", order);
        return "admin/orders/detail";
    }

    @PostMapping("/confirm/{id}")
    public String confirmOrder(@PathVariable Long id,
                               HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = getOrder(id);

        if (!Order.STATUS_PENDING.equals(order.getStatus())) {
            return "redirect:/admin/orders/" + id;
        }

        order.setStatus(Order.STATUS_CONFIRMED);

        if (Order.METHOD_COD.equals(order.getPaymentMethod())) {
            order.setPaymentStatus(Order.PAYMENT_COD);
        }

        orderRepository.save(order);

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/shipping/{id}")
    public String shippingOrder(@PathVariable Long id,
                                HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = getOrder(id);

        if (!Order.STATUS_CONFIRMED.equals(order.getStatus())) {
            return "redirect:/admin/orders/" + id;
        }

        order.setStatus(Order.STATUS_SHIPPING);
        orderRepository.save(order);

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/complete/{id}")
    public String completeOrder(@PathVariable Long id,
                                HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = getOrder(id);

        if (!Order.STATUS_SHIPPING.equals(order.getStatus())
                && !Order.STATUS_CONFIRMED.equals(order.getStatus())) {
            return "redirect:/admin/orders/" + id;
        }

        order.setStatus(Order.STATUS_COMPLETED);

        if (Order.METHOD_COD.equals(order.getPaymentMethod())) {
            order.setPaymentStatus(Order.PAYMENT_PAID);
        }

        orderRepository.save(order);

        sendBillIfPossible(order);

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Long id,
                              HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = getOrder(id);

        if (Order.STATUS_COMPLETED.equals(order.getStatus())
                || Order.STATUS_CANCELLED.equals(order.getStatus())) {
            return "redirect:/admin/orders/" + id;
        }

        restoreStock(order);

        order.setStatus(Order.STATUS_CANCELLED);
        orderRepository.save(order);

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/confirm-payment/{id}")
    public String confirmPayment(@PathVariable Long id,
                                 HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = getOrder(id);

        order.setPaymentStatus(Order.PAYMENT_PAID);

        if (Order.STATUS_PENDING.equals(order.getStatus())) {
            order.setStatus(Order.STATUS_CONFIRMED);
        }

        orderRepository.save(order);

        sendBillIfPossible(order);

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = getOrder(id);

        if (Order.STATUS_CONFIRMED.equals(status)) {
            order.setStatus(Order.STATUS_CONFIRMED);
        } else if (Order.STATUS_SHIPPING.equals(status)) {
            order.setStatus(Order.STATUS_SHIPPING);
        } else if (Order.STATUS_COMPLETED.equals(status)) {
            order.setStatus(Order.STATUS_COMPLETED);

            if (Order.METHOD_COD.equals(order.getPaymentMethod())) {
                order.setPaymentStatus(Order.PAYMENT_PAID);
            }

            sendBillIfPossible(order);
        } else if (Order.STATUS_CANCELLED.equals(status)) {
            if (!Order.STATUS_COMPLETED.equals(order.getStatus())
                    && !Order.STATUS_CANCELLED.equals(order.getStatus())) {
                restoreStock(order);
                order.setStatus(Order.STATUS_CANCELLED);
            }
        }

        orderRepository.save(order);

        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = getOrder(id);

        if (!Order.STATUS_COMPLETED.equals(order.getStatus())
                && !Order.STATUS_CANCELLED.equals(order.getStatus())) {
            restoreStock(order);
        }

        orderRepository.deleteById(id);

        return "redirect:/admin/orders";
    }

    private Order getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
    }

    private void restoreStock(Order order) {
        if (order.getOrderDetails() == null) {
            return;
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            if (detail.getProduct() == null) {
                continue;
            }

            Product product = detail.getProduct();
            int currentStock = product.getQuantity() != null ? product.getQuantity() : 0;
            int quantity = detail.getQuantity();

            product.setQuantity(currentStock + quantity);
            productRepository.save(product);
        }
    }

    private void sendBillIfPossible(Order order) {
        if (order.getUser() == null) {
            return;
        }

        if (order.getUser().getEmail() == null || order.getUser().getEmail().isBlank()) {
            return;
        }

        try {
            emailService.sendPaymentBill(order.getUser().getEmail(), order);
        } catch (Exception e) {
            System.out.println("Gửi email hóa đơn thất bại: " + e.getMessage());
        }
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }
}