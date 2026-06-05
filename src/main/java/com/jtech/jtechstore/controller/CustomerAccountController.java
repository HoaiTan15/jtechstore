package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.repository.AppUserRepository;
import com.jtech.jtechstore.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/tai-khoan")
public class CustomerAccountController {

    private final AppUserRepository appUserRepository;
    private final OrderRepository orderRepository;

    public CustomerAccountController(AppUserRepository appUserRepository,
                                     OrderRepository orderRepository) {
        this.appUserRepository = appUserRepository;
        this.orderRepository = orderRepository;
    }

    private AppUser getCurrentUser(HttpSession session) {
        return (AppUser) session.getAttribute("currentUser");
    }

    @GetMapping
    public String accountHome(HttpSession session, Model model) {
        AppUser currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        List<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(currentUser.getId());

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrders", orders.size());

        return "customer/account";
    }

    @GetMapping("/lich-su")
    public String orderHistory(HttpSession session, Model model) {
        AppUser currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("orders", orderRepository.findByUserIdOrderByOrderDateDesc(currentUser.getId()));

        return "customer/order-history";
    }

    @GetMapping("/don-hang/{id}")
    public String orderDetail(@PathVariable Long id,
                              HttpSession session,
                              Model model) {
        AppUser currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền xem đơn hàng này");
        }

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("order", order);

        return "customer/order-detail";
    }

    @GetMapping("/cap-nhat")
    public String updateProfileForm(HttpSession session, Model model) {
        AppUser currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "customer/profile";
    }

    @PostMapping("/cap-nhat")
    public String updateProfile(@ModelAttribute AppUser formUser,
                                HttpSession session,
                                Model model) {
        AppUser currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        AppUser user = appUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        user.setFullName(formUser.getFullName());
        user.setPhone(formUser.getPhone());
        user.setEmail(formUser.getEmail());

        AppUser savedUser = appUserRepository.save(user);
        session.setAttribute("currentUser", savedUser);

        model.addAttribute("user", savedUser);
        model.addAttribute("success", "Cập nhật thông tin thành công");

        return "customer/profile";
    }

    @GetMapping("/doi-mat-khau")
    public String changePasswordForm(HttpSession session, Model model) {
        AppUser currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("currentUser", currentUser);
        return "customer/change-password";
    }

    @PostMapping("/doi-mat-khau")
    public String changePassword(@RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 Model model) {
        AppUser currentUser = getCurrentUser(session);

        if (currentUser == null) {
            return "redirect:/login";
        }

        AppUser user = appUserRepository.findById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        if (!user.getPassword().equals(oldPassword)) {
            model.addAttribute("error", "Mật khẩu cũ không đúng");
            return "customer/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "customer/change-password";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
            return "customer/change-password";
        }

        user.setPassword(newPassword);
        AppUser savedUser = appUserRepository.save(user);
        session.setAttribute("currentUser", savedUser);

        model.addAttribute("success", "Đổi mật khẩu thành công");

        return "customer/change-password";
    }
}