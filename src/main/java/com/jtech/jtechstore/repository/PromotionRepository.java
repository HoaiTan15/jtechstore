package com.jtech.jtechstore.repository;

import com.jtech.jtechstore.model.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
}