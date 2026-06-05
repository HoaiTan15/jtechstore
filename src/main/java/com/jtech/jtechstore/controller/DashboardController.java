package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.repository.CategoryRepository;
import com.jtech.jtechstore.repository.OrderRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import com.jtech.jtechstore.repository.PromotionRepository;
import com.jtech.jtechstore.repository.AppUserRepository;
import jakarta.servlet.http.HttpSession;
import com.jtech.jtechstore.model.AppUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;
    private final AppUserRepository appUserRepository;

    public DashboardController(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               OrderRepository orderRepository,
                               PromotionRepository promotionRepository,
                               AppUserRepository appUserRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.promotionRepository = promotionRepository;
        this.appUserRepository = appUserRepository;
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model,
                            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("totalProducts", productRepository.count());
        model.addAttribute("totalCategories", categoryRepository.count());
        model.addAttribute("totalOrders", orderRepository.count());
        model.addAttribute("totalPromotions", promotionRepository.count());
        model.addAttribute("totalCustomers", appUserRepository.findByRoleOrderByIdDesc("USER").size());
        model.addAttribute("totalRevenue", orderRepository.getTotalRevenue());
        model.addAttribute("totalStock", productRepository.getTotalStock());

        List<Object[]> categoryStats = productRepository.countProductsByCategory();
        List<Object[]> brandStats = productRepository.countProductsByBrand();

        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("brandStats", brandStats);

        return "admin/dashboard";
    }
}