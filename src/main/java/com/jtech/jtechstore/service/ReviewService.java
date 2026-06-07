package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.model.Review;
import com.jtech.jtechstore.repository.AppUserRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import com.jtech.jtechstore.repository.ReviewRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         ProductRepository productRepository,
                         AppUserRepository appUserRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.appUserRepository = appUserRepository;
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
        Double average = reviewRepository.getAverageRatingByProductId(productId);
        return average != null ? average : 0.0;
    }

    public Long countReviews(Long productId) {
        Long count = reviewRepository.countByProductIdNative(productId);
        return count != null ? count : 0L;
    }

    public Long countReviewsByRating(Long productId, Integer rating) {
        Long count = reviewRepository.countByProductIdAndRatingNative(productId, rating);
        return count != null ? count : 0L;
    }

    public List<Review> getAllReviewsByRating(Integer rating) {
        if (rating == null) {
            return reviewRepository.findAllByOrderByReviewDateDesc();
        }

        return reviewRepository.findByRatingOrderByReviewDateDesc(rating);
    }

    public Long countAllReviews() {
        return reviewRepository.count();
    }

    public Long countAllReviewsByRating(Integer rating) {
        if (rating == null) {
            return 0L;
        }

        Long count = reviewRepository.countByRatingNative(rating);
        return count != null ? count : 0L;
    }

    public boolean canReview(AppUser user, Long productId) {
        if (user == null || user.getId() == null || productId == null) {
            return false;
        }

        Long paidOrderCount = reviewRepository.countPaidOrderByUserAndProduct(user.getId(), productId);

        if (paidOrderCount == null || paidOrderCount == 0) {
            return false;
        }

        return !reviewRepository.existsByUserIdAndProductId(user.getId(), productId);
    }

    @Transactional
    public void addReview(Long productId, Review formReview, AppUser sessionUser) {
        if (sessionUser == null || sessionUser.getId() == null) {
            throw new RuntimeException("Bạn cần đăng nhập để đánh giá.");
        }

        if (productId == null) {
            throw new RuntimeException("Không tìm thấy sản phẩm cần đánh giá.");
        }

        if (!canReview(sessionUser, productId)) {
            throw new RuntimeException("Bạn cần mua và thanh toán sản phẩm này trước khi đánh giá, hoặc bạn đã đánh giá sản phẩm này rồi.");
        }

        if (formReview == null) {
            throw new RuntimeException("Dữ liệu đánh giá không hợp lệ.");
        }

        if (formReview.getRating() == null || formReview.getRating() < 1 || formReview.getRating() > 5) {
            throw new RuntimeException("Số sao đánh giá phải từ 1 đến 5.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm."));

        AppUser user = appUserRepository.findById(sessionUser.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản."));

        Review review = new Review();
        review.setId(null);
        review.setProduct(product);
        review.setUser(user);
        review.setRating(formReview.getRating());
        review.setComment(formReview.getComment());
        review.setReviewDate(LocalDateTime.now());

        review.setCustomerName(user.getFullName() != null && !user.getFullName().isBlank()
                ? user.getFullName()
                : user.getUsername());

        reviewRepository.save(review);
    }

    public void delete(Long id) {
        reviewRepository.deleteById(id);
    }
}