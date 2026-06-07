package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.repository.OrderRepository;
import com.jtech.jtechstore.service.CartService;
import com.jtech.jtechstore.service.EmailService;
import com.jtech.jtechstore.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/checkout")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    public OrderController(OrderService orderService,
                           CartService cartService,
                           OrderRepository orderRepository,
                           EmailService emailService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.orderRepository = orderRepository;
        this.emailService = emailService;
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

        addCheckoutModel(model, currentUser);

        return "checkout";
    }

    @PostMapping
    public String checkout(@RequestParam String customerName,
                           @RequestParam String phone,
                           @RequestParam String address,
                           @RequestParam String paymentMethod,
                           @RequestParam(required = false) String couponCode,
                           HttpSession session,
                           Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (cartService.getCartItems().isEmpty()) {
            return "redirect:/cart";
        }

        try {
            Order order = orderService.checkout(customerName, phone, address, paymentMethod, couponCode, currentUser);

            model.addAttribute("order", order);
            model.addAttribute("currentUser", currentUser);

            return "order-success";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());

            model.addAttribute("customerName", customerName);
            model.addAttribute("phone", phone);
            model.addAttribute("address", address);
            model.addAttribute("paymentMethod", paymentMethod);
            model.addAttribute("couponCode", couponCode);

            addCheckoutModel(model, currentUser);

            return "checkout";
        }
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

        order.setPaymentStatus(Order.PAYMENT_PAID);
        order.setStatus(Order.STATUS_CONFIRMED);
        orderRepository.save(order);

        if (currentUser.getEmail() != null && !currentUser.getEmail().isBlank()) {
            try {
                emailService.sendPaymentBill(currentUser.getEmail(), order);
            } catch (Exception e) {
                System.out.println("Gửi email hóa đơn thất bại: " + e.getMessage());
            }
        }

        model.addAttribute("order", order);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("success", "Xác nhận thanh toán thành công");

        return "order-success";
    }

    private void addCheckoutModel(Model model, AppUser currentUser) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());
        model.addAttribute("currentUser", currentUser);
    }
}