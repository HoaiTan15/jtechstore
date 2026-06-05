package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.repository.OrderRepository;
import com.jtech.jtechstore.service.CartService;
import com.jtech.jtechstore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import com.jtech.jtechstore.model.AppUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/checkout")
public class OrderController {
    private final OrderService orderService;
    private final CartService cartService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService,
                           CartService cartService,
                           OrderRepository orderRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String checkoutForm(Model model, HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());
        model.addAttribute("currentUser", currentUser);

        return "checkout";
    }

    @PostMapping
    public String checkout(@RequestParam String customerName,
                           @RequestParam String phone,
                           @RequestParam String address,
                           @RequestParam String paymentMethod,
                           HttpSession session,
                           Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        Order order = orderService.checkout(customerName, phone, address, paymentMethod, currentUser);

        model.addAttribute("order", order);
        model.addAttribute("currentUser", currentUser);

        return "order-success";
    }

    @PostMapping("/confirm-payment/{id}")
    public String confirmPayment(@PathVariable Long id,
                                 HttpSession session,
                                 Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xác nhận đơn hàng này");
        }

        order.setPaymentStatus("Đã thanh toán");
        order.setStatus("Đã xác nhận");
        orderRepository.save(order);

        model.addAttribute("order", order);
        model.addAttribute("currentUser", currentUser);

        return "order-success";
    }
}