package com.jtech.jtechstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jtech.jtechstore.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}