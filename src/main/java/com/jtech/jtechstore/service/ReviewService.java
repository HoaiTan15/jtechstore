package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.model.Review;
import com.jtech.jtechstore.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import com.jtech.jtechstore.model.AppUser;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductService productService;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.productService = productService;
    }

    public List<Review> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProductIdOrderByReviewDateDesc(productId);
    }

    public List<Review> getReviewsByProductAndRating(Long productId, Integer rating) {
        if (rating == null) {
            return getReviewsByProduct(productId);
        }

        return reviewRepository.findByProductIdAndRatingOrderByReviewDateDesc(productId, rating);
    }

    public Double getAverageRating(Long productId) {
        return reviewRepository.getAverageRatingByProductId(productId);
    }

    public Long countReviews(Long productId) {
        return reviewRepository.countByProductIdNative(productId);
    }

    public Long countReviewsByRating(Long productId, Integer rating) {
        return reviewRepository.countByProductIdAndRatingNative(productId, rating);
    }

    public boolean canReview(AppUser user, Long productId) {
        if (user == null) {
            return false;
        }

        Long paidOrderCount = reviewRepository.countPaidOrderByUserAndProduct(user.getId(), productId);

        if (paidOrderCount == null || paidOrderCount == 0) {
            return false;
        }

        return !reviewRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    public void addReview(Long productId, Review review, AppUser user) {
        if (user == null) {
            throw new RuntimeException("Bạn cần đăng nhập để đánh giá.");
        }

        if (!canReview(user, productId)) {
            throw new RuntimeException("Bạn cần mua và thanh toán sản phẩm này trước khi đánh giá, hoặc bạn đã đánh giá sản phẩm này rồi.");
        }

        Product product = productService.getById(productId);

        review.setProduct(product);
        review.setUser(user);
        review.setCustomerName(user.getFullName() != null && !user.getFullName().isBlank()
                ? user.getFullName()
                : user.getUsername());
        review.setReviewDate(LocalDateTime.now());

        reviewRepository.save(review);
    }

    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }
}