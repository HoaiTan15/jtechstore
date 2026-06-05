package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import com.jtech.jtechstore.model.AppUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AppUserRepository appUserRepository;

    public AdminUserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping
    public String list(Model model,
                       HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("users", appUserRepository.findByRoleOrderByIdDesc("USER"));
        return "admin/users/list";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        model.addAttribute("user", user);
        return "admin/users/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute AppUser user,
                       HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        AppUser oldUser = appUserRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        oldUser.setFullName(user.getFullName());
        oldUser.setPhone(user.getPhone());
        oldUser.setEmail(user.getEmail());
        oldUser.setRole(user.getRole());
        oldUser.setEnabled(user.getEnabled());

        appUserRepository.save(oldUser);

        return "redirect:/admin/users";
    }

    @GetMapping("/lock/{id}")
    public String lock(@PathVariable Long id,
                       HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        user.setEnabled(false);
        appUserRepository.save(user);

        return "redirect:/admin/users";
    }

    @GetMapping("/unlock/{id}")
    public String unlock(@PathVariable Long id,
                         HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        user.setEnabled(true);
        appUserRepository.save(user);

        return "redirect:/admin/users";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản"));

        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/users";
        }

        appUserRepository.deleteById(id);

        return "redirect:/admin/users";
    }
}