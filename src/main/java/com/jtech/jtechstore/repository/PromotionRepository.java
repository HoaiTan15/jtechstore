package com.jtech.jtechstore.repository;

import com.jtech.jtechstore.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    Optional<Promotion> findByCouponCode(String couponCode);

    boolean existsByCouponCode(String couponCode);

    List<Promotion> findAllByOrderByIdDesc();
}