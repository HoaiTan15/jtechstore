package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.Promotion;
import com.jtech.jtechstore.service.PromotionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.jtech.jtechstore.model.AppUser;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("promotions", promotionService.getAll());
        return "admin/promotions/list";
    }

    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("promotion", new Promotion());
        return "admin/promotions/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Promotion promotion,
                       BindingResult result,
                       HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        if (result.hasErrors()) {
            return "admin/promotions/form";
        }

        promotionService.save(promotion);
        return "redirect:/admin/promotions";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("promotion", promotionService.getById(id));
        return "admin/promotions/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        promotionService.delete(id);
        return "redirect:/admin/promotions";
    }
}