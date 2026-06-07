package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.Promotion;
import com.jtech.jtechstore.repository.PromotionRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromotionService {
    private final PromotionRepository promotionRepository;

    public PromotionService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    public List<Promotion> getAll() {
        return promotionRepository.findAllByOrderByIdDesc();
    }

    public Promotion getById(Long id) {
        return promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi"));
    }

    public void save(Promotion promotion) {
        normalizePromotion(promotion);

        if (promotion.getCouponCode() != null && !promotion.getCouponCode().isBlank()) {
            Promotion existing = promotionRepository.findByCouponCode(promotion.getCouponCode())
                    .orElse(null);

            if (existing != null && !existing.getId().equals(promotion.getId())) {
                throw new RuntimeException("Mã coupon đã tồn tại");
            }
        }

        promotionRepository.save(promotion);
    }

    public Promotion getValidCoupon(String couponCode, double orderTotal) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng nhập mã giảm giá");
        }

        String cleanCode = couponCode.trim().toUpperCase();

        Promotion coupon = promotionRepository.findByCouponCode(cleanCode)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

        if (!coupon.canUseCoupon(orderTotal)) {
            if (!coupon.isValidNow()) {
                throw new RuntimeException("Mã giảm giá đã hết hạn hoặc chưa được kích hoạt");
            }

            if (coupon.getMinOrderAmount() != null && orderTotal < coupon.getMinOrderAmount()) {
                throw new RuntimeException(
                        "Đơn hàng phải từ " + String.format("%,.0f", coupon.getMinOrderAmount()) + " VNĐ mới dùng được mã này"
                );
            }

            if (coupon.getUsageLimit() != null && coupon.getUsageLimit() > 0) {
                int usedCount = coupon.getUsedCount() != null ? coupon.getUsedCount() : 0;

                if (usedCount >= coupon.getUsageLimit()) {
                    throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
                }
            }

            throw new RuntimeException("Mã giảm giá không hợp lệ");
        }

        return coupon;
    }

    public double calculateCouponDiscount(String couponCode, double orderTotal) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return 0;
        }

        Promotion coupon = getValidCoupon(couponCode, orderTotal);
        return coupon.calculateCouponDiscount(orderTotal);
    }

    public void increaseCouponUsedCount(String couponCode) {
        if (couponCode == null || couponCode.trim().isEmpty()) {
            return;
        }

        Promotion coupon = promotionRepository.findByCouponCode(couponCode.trim().toUpperCase())
                .orElse(null);

        if (coupon == null) {
            return;
        }

        coupon.increaseUsedCount();
        promotionRepository.save(coupon);
    }

    public void delete(Long id) {
        promotionRepository.deleteById(id);
    }

    private void normalizePromotion(Promotion promotion) {
        if (promotion.getActive() == null) {
            promotion.setActive(false);
        }

        if (promotion.getDiscountPercent() == null) {
            promotion.setDiscountPercent(0.0);
        }

        if (promotion.getCouponCode() != null) {
            String cleanCode = promotion.getCouponCode().trim().toUpperCase();

            if (cleanCode.isBlank()) {
                promotion.setCouponCode(null);
            } else {
                promotion.setCouponCode(cleanCode);
            }
        }

        if (promotion.getDiscountType() == null || promotion.getDiscountType().isBlank()) {
            promotion.setDiscountType(Promotion.DISCOUNT_PERCENT);
        }

        if (promotion.getCouponValue() == null) {
            promotion.setCouponValue(0.0);
        }

        if (promotion.getMinOrderAmount() == null) {
            promotion.setMinOrderAmount(0.0);
        }

        if (promotion.getUsedCount() == null) {
            promotion.setUsedCount(0);
        }
    }
}