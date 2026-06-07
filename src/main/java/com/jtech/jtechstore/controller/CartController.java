package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.service.CartService;
import com.jtech.jtechstore.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
                            HttpSession session,
                            Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            Product product = productService.getById(productId);
            cartService.addToCart(product);

            model.addAttribute("success", "Đã thêm sản phẩm vào giỏ hàng");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());

        return "cart";
    }

    @PostMapping("/update/{productId}")
    public String updateQuantity(@PathVariable Long productId,
                                 @RequestParam Integer quantity,
                                 HttpSession session,
                                 Model model) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        try {
            cartService.updateQuantity(productId, quantity);
            model.addAttribute("success", "Cập nhật giỏ hàng thành công");
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
        }

        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());

        return "cart";
    }

    @GetMapping("/remove/{productId}")
    public String remove(@PathVariable Long productId,
                         Model model) {
        cartService.removeFromCart(productId);

        model.addAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng");
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());

        return "cart";
    }

    @GetMapping("/clear")
    public String clearCart(Model model) {
        cartService.clearCart();

        model.addAttribute("success", "Đã xóa toàn bộ giỏ hàng");
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());

        return "cart";
    }
}