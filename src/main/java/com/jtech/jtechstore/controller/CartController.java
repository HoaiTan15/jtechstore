package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.service.CartService;
import com.jtech.jtechstore.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.jtech.jtechstore.model.AppUser;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {
    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService,
                          ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping
    public String viewCart(Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());
        return "cart";
    }

    @GetMapping("/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        cartService.addToCart(productService.getById(productId));
        return "redirect:/cart";
    }

    @GetMapping("/remove/{productId}")
    public String remove(@PathVariable Long productId) {
        cartService.removeFromCart(productId);
        return "redirect:/cart";
    }
}