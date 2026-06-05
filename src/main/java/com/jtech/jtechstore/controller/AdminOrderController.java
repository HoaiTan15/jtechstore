package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.jtech.jtechstore.model.AppUser;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderRepository orderRepository;

    public AdminOrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
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

    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam String status,
                               HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        order.setStatus(status);
        orderRepository.save(order);

        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/confirm-payment/{id}")
    public String confirmPayment(@PathVariable Long id,
                                 HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        order.setPaymentStatus("Đã thanh toán");
        order.setStatus("Đã xác nhận");
        orderRepository.save(order);

        return "redirect:/admin/orders/" + id;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        orderRepository.deleteById(id);
        return "redirect:/admin/orders";
    }
}