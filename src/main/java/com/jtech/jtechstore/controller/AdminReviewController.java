package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.service.ReviewService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ReviewService reviewService;

    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public String listReviews(@RequestParam(required = false) Integer rating,
                              HttpSession session,
                              Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("reviews", reviewService.getAllReviewsByRating(rating));
        model.addAttribute("selectedRating", rating);

        model.addAttribute("totalReviews", reviewService.countAllReviews());
        model.addAttribute("rating5Count", reviewService.countAllReviewsByRating(5));
        model.addAttribute("rating4Count", reviewService.countAllReviewsByRating(4));
        model.addAttribute("rating3Count", reviewService.countAllReviewsByRating(3));
        model.addAttribute("rating2Count", reviewService.countAllReviewsByRating(2));
        model.addAttribute("rating1Count", reviewService.countAllReviewsByRating(1));

        return "admin/reviews/list";
    }

    @GetMapping("/delete/{id}")
    public String deleteReview(@PathVariable Long id,
                               HttpSession session,
                               Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        reviewService.delete(id);

        return "redirect:/admin/reviews";
    }

    private boolean isAdmin(HttpSession session) {
        AppUser currentUser = (AppUser) session.getAttribute("currentUser");
        return currentUser != null && "ADMIN".equals(currentUser.getRole());
    }
}