package com.jtech.jtechstore.repository;

import com.jtech.jtechstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders", nativeQuery = true)
    Double getTotalRevenue();

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
}