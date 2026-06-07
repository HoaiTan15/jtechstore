package com.jtech.jtechstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    @NotNull(message = "Giá không được để trống")
    @Min(value = 0, message = "Giá phải lớn hơn hoặc bằng 0")
    private Double price;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String imageUrl;

    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 0, message = "Số lượng phải lớn hơn hoặc bằng 0")
    private Integer quantity = 0;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String brand;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String cpu;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String ram;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String storage;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String screen;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String warranty;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "promotion_id")
    private Promotion promotion;

    public double getSalePrice() {
        if (price == null) {
            return 0;
        }

        if (promotion == null || !promotion.isValidNow()) {
            return price;
        }

        Double discount = promotion.getDiscountPercent();

        if (discount == null || discount <= 0) {
            return price;
        }

        return price - (price * discount / 100);
    }

    public double getDiscountPercent() {
        if (promotion == null || !promotion.isValidNow() || promotion.getDiscountPercent() == null) {
            return 0;
        }

        return promotion.getDiscountPercent();
    }

    public boolean isInStock() {
        return quantity != null && quantity > 0;
    }

    public boolean isLowStock() {
        return quantity != null && quantity > 0 && quantity < 5;
    }

    public boolean canBuy(int buyQuantity) {
        if (buyQuantity <= 0) {
            return false;
        }

        return quantity != null && quantity >= buyQuantity;
    }
}