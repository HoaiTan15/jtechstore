package com.jtech.jtechstore.repository;

import com.jtech.jtechstore.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByNameContainingIgnoreCase(String keyword);

    @Query(value = "SELECT COALESCE(SUM(quantity), 0) FROM products", nativeQuery = true)
    Long getTotalStock();

    @Query(value = """
            SELECT c.name, COUNT(p.id)
            FROM products p
            JOIN categories c ON p.category_id = c.id
            GROUP BY c.name
            """, nativeQuery = true)
    List<Object[]> countProductsByCategory();

    @Query(value = """
            SELECT brand, COUNT(id)
            FROM products
            WHERE brand IS NOT NULL AND brand <> ''
            GROUP BY brand
            """, nativeQuery = true)
    List<Object[]> countProductsByBrand();

    @Query(value = """
            SELECT DISTINCT brand
            FROM products
            WHERE brand IS NOT NULL AND brand <> ''
            ORDER BY brand
            """, nativeQuery = true)
    List<String> findDistinctBrands();
}