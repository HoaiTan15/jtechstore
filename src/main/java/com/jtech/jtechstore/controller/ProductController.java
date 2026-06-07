package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.model.Review;
import com.jtech.jtechstore.service.CategoryService;
import com.jtech.jtechstore.service.ProductService;
import com.jtech.jtechstore.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             ReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
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
            addProductDetailModel(model, product, id, review, currentUser, null);
            return "product-detail";
        }

        try {
            reviewService.addReview(id, review, currentUser);
        } catch (RuntimeException e) {
            addProductDetailModel(model, product, id, review, currentUser, e.getMessage());
            return "product-detail";
        }

        return "redirect:/products/" + id;
    }

    private void addProductDetailModel(Model model,
                                       Product product,
                                       Long productId,
                                       Review review,
                                       AppUser currentUser,
                                       String reviewError) {
        model.addAttribute("product", product);
        model.addAttribute("review", review);

        model.addAttribute("reviews", reviewService.getReviewsByProduct(productId));
        model.addAttribute("averageRating", reviewService.getAverageRating(productId));
        model.addAttribute("reviewCount", reviewService.countReviews(productId));

        model.addAttribute("rating5Count", reviewService.countReviewsByRating(productId, 5));
        model.addAttribute("rating4Count", reviewService.countReviewsByRating(productId, 4));
        model.addAttribute("rating3Count", reviewService.countReviewsByRating(productId, 3));
        model.addAttribute("rating2Count", reviewService.countReviewsByRating(productId, 2));
        model.addAttribute("rating1Count", reviewService.countReviewsByRating(productId, 1));

        model.addAttribute("selectedRating", null);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("canReview", reviewService.canReview(currentUser, productId));

        if (reviewError != null) {
            model.addAttribute("reviewError", reviewError);
        }

        if (product.getCategory() != null) {
            model.addAttribute("relatedProducts",
                    productService.getRelatedProducts(product.getCategory().getId(), product.getId()));
        }
    }
}