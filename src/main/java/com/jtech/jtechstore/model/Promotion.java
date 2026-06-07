package com.jtech.jtechstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "promotions")
public class Promotion {
    public static final String DISCOUNT_PERCENT = "PERCENT";
    public static final String DISCOUNT_AMOUNT = "AMOUNT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    /*
     * Dùng cho giảm giá trực tiếp trên sản phẩm.
     * Ví dụ: sản phẩm A được gắn khuyến mãi giảm 10%.
     */
    @Min(value = 0, message = "Phần trăm giảm giá không được âm")
    @Max(value = 100, message = "Phần trăm giảm giá không được vượt quá 100")
    private Double discountPercent;

    /*
     * Dùng cho mã coupon nhập lúc thanh toán.
     * Ví dụ:
     * couponCode = JTECH10
     * discountType = PERCENT
     * couponValue = 10
     * minOrderAmount = 500000
     */
    @Column(columnDefinition = "NVARCHAR(100)", unique = true)
    private String couponCode;

    @Column(columnDefinition = "NVARCHAR(50)")
    private String discountType = DISCOUNT_PERCENT;

    private Double couponValue;

    private Double minOrderAmount = 0.0;

    private Integer usageLimit;

    private Integer usedCount = 0;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean active = true;

    @OneToMany(mappedBy = "promotion")
    private List<Product> products;

    public boolean isValidNow() {
        LocalDate today = LocalDate.now();

        if (active == null || !active) {
            return false;
        }

        if (startDate != null && today.isBefore(startDate)) {
            return false;
        }

        if (endDate != null && today.isAfter(endDate)) {
            return false;
        }

        return true;
    }

    public boolean isCoupon() {
        return couponCode != null && !couponCode.trim().isEmpty();
    }

    public boolean canUseCoupon(double orderTotal) {
        if (!isCoupon()) {
            return false;
        }

        if (!isValidNow()) {
            return false;
        }

        if (minOrderAmount != null && orderTotal < minOrderAmount) {
            return false;
        }

        if (usageLimit != null && usageLimit > 0) {
            int currentUsed = usedCount != null ? usedCount : 0;
            return currentUsed < usageLimit;
        }

        return true;
    }

    public double calculateCouponDiscount(double orderTotal) {
        if (!canUseCoupon(orderTotal)) {
            return 0;
        }

        if (couponValue == null || couponValue <= 0) {
            return 0;
        }

        if (DISCOUNT_AMOUNT.equals(discountType)) {
            return Math.min(couponValue, orderTotal);
        }

        double percentDiscount = orderTotal * couponValue / 100;
        return Math.min(percentDiscount, orderTotal);
    }

    public void increaseUsedCount() {
        if (usedCount == null) {
            usedCount = 0;
        }

        usedCount++;
    }
}