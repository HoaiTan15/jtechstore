package com.jtech.jtechstore.repository;

import com.jtech.jtechstore.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query(value = "SELECT COALESCE(SUM(total_amount), 0) FROM orders", nativeQuery = true)
    Double getTotalRevenue();

    @Query(value = """
            SELECT COALESCE(SUM(total_amount), 0)
            FROM orders
            WHERE status = :status
              AND order_date >= :startDate
              AND order_date < :endDate
            """, nativeQuery = true)
    Double getRevenueByStatusAndDateRange(@Param("status") String status,
                                          @Param("startDate") LocalDateTime startDate,
                                          @Param("endDate") LocalDateTime endDate);

    @Query(value = """
            SELECT COUNT(*)
            FROM orders
            WHERE status = :status
              AND order_date >= :startDate
              AND order_date < :endDate
            """, nativeQuery = true)
    Long countOrdersByStatusAndDateRange(@Param("status") String status,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    List<Order> findAllByOrderByOrderDateDesc();

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
}