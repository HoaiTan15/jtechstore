package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.jtech.jtechstore.model.Category;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    private boolean isAdmin(HttpSession session) {
        Object user = session.getAttribute("currentUser");
        return user instanceof com.jtech.jtechstore.model.AppUser appUser
                && "ADMIN".equals(appUser.getRole());
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("categories", categoryService.getAll());
        return "admin/categories/list";
    }

    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("category", new Category());
        return "admin/categories/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute Category category,
                       BindingResult result,
                       HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        if (result.hasErrors()) {
            return "admin/categories/form";
        }

        categoryService.save(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("category", categoryService.getById(id));
        return "admin/categories/form";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        categoryService.delete(id);
        return "redirect:/admin/categories";
    }
}