package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.repository.AppUserRepository;
import com.jtech.jtechstore.repository.CategoryRepository;
import com.jtech.jtechstore.repository.OrderRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import com.jtech.jtechstore.repository.PromotionRepository;
import com.jtech.jtechstore.repository.ReviewRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final OrderRepository orderRepository;
    private final PromotionRepository promotionRepository;
    private final AppUserRepository appUserRepository;
    private final ReviewRepository reviewRepository;

    public DashboardController(ProductRepository productRepository,
                               CategoryRepository categoryRepository,
                               OrderRepository orderRepository,
                               PromotionRepository promotionRepository,
                               AppUserRepository appUserRepository,
                               ReviewRepository reviewRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.orderRepository = orderRepository;
        this.promotionRepository = promotionRepository;
        this.appUserRepository = appUserRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model,
                            HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Product> products = productRepository.findAll();
        List<Order> orders = orderRepository.findAll();
        List<AppUser> customers = appUserRepository.findByRoleOrderByIdDesc("USER");

        long totalProducts = products.size();
        long totalCategories = categoryRepository.count();
        long totalOrders = orders.size();
        long totalPromotions = promotionRepository.count();
        long totalCustomers = customers.size();
        long totalReviews = reviewRepository.count();

        long pendingOrders = orders.stream()
                .filter(o -> Order.STATUS_PENDING.equals(o.getStatus()))
                .count();

        long confirmedOrders = orders.stream()
                .filter(o -> Order.STATUS_CONFIRMED.equals(o.getStatus()))
                .count();

        long shippingOrders = orders.stream()
                .filter(o -> Order.STATUS_SHIPPING.equals(o.getStatus()))
                .count();

        long completedOrders = orders.stream()
                .filter(o -> Order.STATUS_COMPLETED.equals(o.getStatus()))
                .count();

        long cancelledOrders = orders.stream()
                .filter(o -> Order.STATUS_CANCELLED.equals(o.getStatus()))
                .count();

        double totalRevenue = orders.stream()
                .filter(o -> Order.STATUS_COMPLETED.equals(o.getStatus()))
                .filter(o -> o.getTotalAmount() != null)
                .mapToDouble(Order::getTotalAmount)
                .sum();

        int totalStock = products.stream()
                .mapToInt(p -> p.getQuantity() != null ? p.getQuantity() : 0)
                .sum();

        long lowStockCount = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                .count();

        long outOfStockCount = products.stream()
                .filter(p -> p.getQuantity() == null || p.getQuantity() <= 0)
                .count();

        Map<String, Long> categoryStats = new LinkedHashMap<>();
        for (Product product : products) {
            String categoryName = product.getCategory() != null ? product.getCategory().getName() : "Chưa phân loại";
            categoryStats.put(categoryName, categoryStats.getOrDefault(categoryName, 0L) + 1);
        }

        Map<String, Long> brandStats = new LinkedHashMap<>();
        for (Product product : products) {
            String brand = product.getBrand() != null && !product.getBrand().isBlank()
                    ? product.getBrand()
                    : "Chưa có thương hiệu";
            brandStats.put(brand, brandStats.getOrDefault(brand, 0L) + 1);
        }

        List<Order> recentOrders = orders.stream()
                .sorted((o1, o2) -> {
                    if (o1.getOrderDate() == null && o2.getOrderDate() == null) return 0;
                    if (o1.getOrderDate() == null) return 1;
                    if (o2.getOrderDate() == null) return -1;
                    return o2.getOrderDate().compareTo(o1.getOrderDate());
                })
                .limit(5)
                .toList();

        List<Product> lowStockProducts = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                .limit(5)
                .toList();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalCategories", totalCategories);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalPromotions", totalPromotions);
        model.addAttribute("totalCustomers", totalCustomers);
        model.addAttribute("totalReviews", totalReviews);

        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("confirmedOrders", confirmedOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalStock", totalStock);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);

        model.addAttribute("categoryStats", categoryStats);
        model.addAttribute("brandStats", brandStats);

        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("lowStockProducts", lowStockProducts);

        return "admin/dashboard";
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }
}