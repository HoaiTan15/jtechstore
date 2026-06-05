package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.service.CategoryService;
import com.jtech.jtechstore.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final ProductService productService;
    private final CategoryService categoryService;

    public HomeController(ProductService productService,
                          CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", productService.getAll(null));
        model.addAttribute("categories", categoryService.getAll());
        return "home";
    }
}