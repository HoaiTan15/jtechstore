package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.model.Review;
import com.jtech.jtechstore.service.CategoryService;
import com.jtech.jtechstore.service.ProductService;
import com.jtech.jtechstore.service.PromotionService;
import com.jtech.jtechstore.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import com.jtech.jtechstore.model.AppUser;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final PromotionService promotionService;
    private final ReviewService reviewService;

    private static final String UPLOAD_DIR = "uploads/products/";

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             PromotionService promotionService,
                             ReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.promotionService = promotionService;
        this.reviewService = reviewService;
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String keyword,
                           @RequestParam(required = false) Long categoryId,
                           @RequestParam(required = false) String brand,
                           @RequestParam(required = false) Double minPrice,
                           @RequestParam(required = false) Double maxPrice,
                           @RequestParam(required = false) String sort,
                           Model model) {
        model.addAttribute("products",
                productService.filterProducts(keyword, categoryId, brand, minPrice, maxPrice, sort));

        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("brands", productService.getBrands());

        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brand", brand);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);

        return "products";
    }

    @GetMapping("/products/{id}")
    public String productDetail(@PathVariable Long id,
                                @RequestParam(required = false) Integer rating,
                                Model model,
                                HttpSession session) {
        Product product = productService.getById(id);
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        model.addAttribute("product", product);
        model.addAttribute("review", new Review());

        model.addAttribute("reviews", reviewService.getReviewsByProductAndRating(id, rating));
        model.addAttribute("averageRating", reviewService.getAverageRating(id));
        model.addAttribute("reviewCount", reviewService.countReviews(id));

        model.addAttribute("rating5Count", reviewService.countReviewsByRating(id, 5));
        model.addAttribute("rating4Count", reviewService.countReviewsByRating(id, 4));
        model.addAttribute("rating3Count", reviewService.countReviewsByRating(id, 3));
        model.addAttribute("rating2Count", reviewService.countReviewsByRating(id, 2));
        model.addAttribute("rating1Count", reviewService.countReviewsByRating(id, 1));

        model.addAttribute("selectedRating", rating);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("canReview", reviewService.canReview(currentUser, id));

        if (product.getCategory() != null) {
            model.addAttribute("relatedProducts",
                    productService.getRelatedProducts(product.getCategory().getId(), product.getId()));
        }

        return "product-detail";
    }

    @PostMapping("/products/{id}/reviews")
    public String addReview(@PathVariable Long id,
                            @Valid @ModelAttribute Review review,
                            BindingResult result,
                            Model model,
                            HttpSession session) {
        Product product = productService.getById(id);
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");

        if (currentUser == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            model.addAttribute("product", product);
            model.addAttribute("review", review);
            model.addAttribute("reviews", reviewService.getReviewsByProduct(id));
            model.addAttribute("averageRating", reviewService.getAverageRating(id));
            model.addAttribute("reviewCount", reviewService.countReviews(id));
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("canReview", reviewService.canReview(currentUser, id));

            if (product.getCategory() != null) {
                model.addAttribute("relatedProducts",
                        productService.getRelatedProducts(product.getCategory().getId(), product.getId()));
            }

            return "product-detail";
        }

        try {
            reviewService.addReview(id, review, currentUser);
        } catch (RuntimeException e) {
            model.addAttribute("product", product);
            model.addAttribute("review", review);
            model.addAttribute("reviews", reviewService.getReviewsByProduct(id));
            model.addAttribute("averageRating", reviewService.getAverageRating(id));
            model.addAttribute("reviewCount", reviewService.countReviews(id));
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("canReview", reviewService.canReview(currentUser, id));
            model.addAttribute("reviewError", e.getMessage());

            if (product.getCategory() != null) {
                model.addAttribute("relatedProducts",
                        productService.getRelatedProducts(product.getCategory().getId(), product.getId()));
            }

            return "product-detail";
        }

        return "redirect:/products/" + id;
    }

    @GetMapping("/admin/products")
    public String adminProducts(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("products", productService.getAll(null));
        return "admin/products/list";
    }

    @GetMapping("/admin/products/add")
    public String addForm(Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("promotions", promotionService.getAll());
        return "admin/products/form";
    }

    @PostMapping("/admin/products/save")
    public String save(@Valid @ModelAttribute Product product,
                       BindingResult result,
                       @RequestParam("imageFile") MultipartFile imageFile,
                       Model model,
                       HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("promotions", promotionService.getAll());
            return "admin/products/form";
        }

        try {
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = saveImage(imageFile);
                product.setImageUrl(imageUrl);
            } else if (product.getId() != null) {
                Product oldProduct = productService.getById(product.getId());
                product.setImageUrl(oldProduct.getImageUrl());
            }
        } catch (Exception e) {
            model.addAttribute("categories", categoryService.getAll());
            model.addAttribute("promotions", promotionService.getAll());
            model.addAttribute("uploadError", "Upload ảnh thất bại: " + e.getMessage());
            return "admin/products/form";
        }

        productService.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        model.addAttribute("product", productService.getById(id));
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("promotions", promotionService.getAll());
        return "admin/products/form";
    }

    @GetMapping("/admin/products/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(session)) return "redirect:/login";

        productService.delete(id);
        return "redirect:/admin/products";
    }

    private String saveImage(MultipartFile imageFile) throws Exception {
        String originalFilename = imageFile.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new RuntimeException("Tên file không hợp lệ");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex >= 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String fileName = UUID.randomUUID() + extension;

        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.copy(imageFile.getInputStream(), filePath);

        return "/" + UPLOAD_DIR + fileName;
    }
}