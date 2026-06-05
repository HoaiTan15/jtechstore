package com.jtech.jtechstore.repository;

import com.jtech.jtechstore.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdOrderByReviewDateDesc(Long productId);

    List<Review> findByProductIdAndRatingOrderByReviewDateDesc(Long productId, Integer rating);

    @Query(value = "SELECT COALESCE(AVG(CAST(rating AS FLOAT)), 0) FROM reviews WHERE product_id = ?1", nativeQuery = true)
    Double getAverageRatingByProductId(Long productId);

    @Query(value = "SELECT COUNT(*) FROM reviews WHERE product_id = ?1", nativeQuery = true)
    Long countByProductIdNative(Long productId);

    @Query(value = "SELECT COUNT(*) FROM reviews WHERE product_id = ?1 AND rating = ?2", nativeQuery = true)
    Long countByProductIdAndRatingNative(Long productId, Integer rating);

    @Query(value = """
            SELECT COUNT(*)
            FROM orders o
            JOIN order_details od ON o.id = od.order_id
            WHERE o.user_id = ?1
              AND od.product_id = ?2
              AND o.payment_status = N'Đã thanh toán'
            """, nativeQuery = true)
    Long countPaidOrderByUserAndProduct(Long userId, Long productId);

    boolean existsByUserIdAndProductId(Long userId, Long productId);
}