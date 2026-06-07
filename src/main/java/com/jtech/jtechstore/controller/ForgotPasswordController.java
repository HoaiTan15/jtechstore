package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.repository.AppUserRepository;
import com.jtech.jtechstore.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;

@Controller
public class ForgotPasswordController {

    private final AppUserRepository appUserRepository;
    private final EmailService emailService;

    public ForgotPasswordController(AppUserRepository appUserRepository,
                                    EmailService emailService) {
        this.appUserRepository = appUserRepository;
        this.emailService = emailService;
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String sendResetCode(@RequestParam String email,
                                HttpSession session,
                                Model model) {
        AppUser user = appUserRepository.findByEmail(email)
                .orElse(null);

        if (user == null) {
            model.addAttribute("error", "Không tìm thấy tài khoản với email này");
            return "forgot-password";
        }

        String code = String.valueOf(100000 + new Random().nextInt(900000));

        user.setResetCode(code);
        user.setResetCodeExpireAt(LocalDateTime.now().plusMinutes(5));
        appUserRepository.save(user);

        emailService.sendResetPasswordCode(email, code);

        session.setAttribute("resetEmail", email);

        return "redirect:/reset-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordForm(HttpSession session, Model model) {
        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            return "redirect:/forgot-password";
        }

        model.addAttribute("email", email);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String code,
                                @RequestParam String newPassword,
                                @RequestParam String confirmPassword,
                                HttpSession session,
                                Model model) {
        String email = (String) session.getAttribute("resetEmail");

        if (email == null) {
            return "redirect:/forgot-password";
        }

        AppUser user = appUserRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Mã xác nhận không đúng");
            return "reset-password";
        }

        if (user.getResetCodeExpireAt() == null || user.getResetCodeExpireAt().isBefore(LocalDateTime.now())) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Mã xác nhận đã hết hạn");
            return "reset-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Mật khẩu xác nhận không khớp");
            return "reset-password";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Mật khẩu mới phải có ít nhất 6 ký tự");
            return "reset-password";
        }

        user.setPassword(newPassword);
        user.setResetCode(null);
        user.setResetCodeExpireAt(null);
        appUserRepository.save(user);

        session.removeAttribute("resetEmail");

        model.addAttribute("success", "Đặt lại mật khẩu thành công. Vui lòng đăng nhập.");
        return "login";
    }
}