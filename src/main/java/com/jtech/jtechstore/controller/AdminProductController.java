package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.repository.CategoryRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import com.jtech.jtechstore.repository.PromotionRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final PromotionRepository promotionRepository;

    public AdminProductController(ProductRepository productRepository,
                                  CategoryRepository categoryRepository,
                                  PromotionRepository promotionRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.promotionRepository = promotionRepository;
    }

    @GetMapping
    public String listProducts(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        long lowStockCount = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                .count();

        long outOfStockCount = products.stream()
                .filter(p -> p.getQuantity() == null || p.getQuantity() <= 0)
                .count();

        model.addAttribute("products", products);
        model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);

        return "admin/products/list";
    }

    @GetMapping("/add")
    public String addForm(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("product", new Product());
        addFormData(model);
        model.addAttribute("pageTitle", "Thêm sản phẩm");

        return "admin/products/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           HttpSession session,
                           Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        model.addAttribute("product", product);
        addFormData(model);
        model.addAttribute("pageTitle", "Cập nhật sản phẩm");

        return "admin/products/form";
    }

    @PostMapping("/save")
    public String saveProduct(@Valid @ModelAttribute("product") Product product,
                              BindingResult result,
                              @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                              HttpSession session,
                              Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        if (product.getQuantity() == null) {
            product.setQuantity(0);
        }

        if (result.hasErrors()) {
            addFormData(model);
            model.addAttribute("pageTitle", product.getId() == null ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
            return "admin/products/form";
        }

        if (product.getPrice() != null && product.getPrice() < 0) {
            model.addAttribute("error", "Giá sản phẩm không được âm");
            addFormData(model);
            model.addAttribute("pageTitle", product.getId() == null ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
            return "admin/products/form";
        }

        if (product.getQuantity() < 0) {
            model.addAttribute("error", "Số lượng tồn kho không được âm");
            addFormData(model);
            model.addAttribute("pageTitle", product.getId() == null ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
            return "admin/products/form";
        }

        if (product.getId() != null) {
            Product oldProduct = productRepository.findById(product.getId()).orElse(null);

            if (oldProduct != null && (product.getImageUrl() == null || product.getImageUrl().isBlank())) {
                product.setImageUrl(oldProduct.getImageUrl());
            }
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = saveImage(imageFile);
                product.setImageUrl("/uploads/products/" + fileName);
            }
        } catch (IOException e) {
            model.addAttribute("error", "Lưu ảnh thất bại: " + e.getMessage());
            addFormData(model);
            model.addAttribute("pageTitle", product.getId() == null ? "Thêm sản phẩm" : "Cập nhật sản phẩm");
            return "admin/products/form";
        }

        productRepository.save(product);

        return "redirect:/admin/products";
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        productRepository.deleteById(id);

        return "redirect:/admin/products";
    }

    @GetMapping("/inventory")
    public String inventory(HttpSession session, Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        List<Product> products = productRepository.findAll(Sort.by(Sort.Direction.ASC, "quantity"));

        List<Product> lowStockProducts = products.stream()
                .filter(p -> p.getQuantity() != null && p.getQuantity() > 0 && p.getQuantity() < 5)
                .toList();

        List<Product> outOfStockProducts = products.stream()
                .filter(p -> p.getQuantity() == null || p.getQuantity() <= 0)
                .toList();

        model.addAttribute("products", products);
        model.addAttribute("lowStockProducts", lowStockProducts);
        model.addAttribute("outOfStockProducts", outOfStockProducts);

        return "admin/products/inventory";
    }

    private void addFormData(Model model) {
        model.addAttribute("categories", categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("promotions", promotionRepository.findAll(Sort.by(Sort.Direction.DESC, "id")));
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        String originalName = imageFile.getOriginalFilename();

        if (originalName == null || originalName.isBlank()) {
            originalName = "product.jpg";
        }

        String cleanName = originalName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
        String fileName = System.currentTimeMillis() + "_" + cleanName;

        Path uploadDir = Paths.get("uploads/products");

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        Path filePath = uploadDir.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }

    private boolean isAdmin(HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }
}