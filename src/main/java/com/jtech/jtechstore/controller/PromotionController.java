package com.jtech.jtechstore.controller;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.Promotion;
import com.jtech.jtechstore.service.PromotionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/promotions")
public class PromotionController {
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @GetMapping
    public String list(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("promotions", promotionService.getAll());
        return "admin/promotions/list";
    }

    @GetMapping("/add")
    public String addForm(Model model, HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        Promotion promotion = new Promotion();
        promotion.setActive(true);
        promotion.setDiscountType(Promotion.DISCOUNT_PERCENT);
        promotion.setMinOrderAmount(0.0);
        promotion.setUsedCount(0);

        model.addAttribute("promotion", promotion);
        model.addAttribute("pageTitle", "Thêm khuyến mãi / coupon");

        return "admin/promotions/form";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        model.addAttribute("promotion", promotionService.getById(id));
        model.addAttribute("pageTitle", "Cập nhật khuyến mãi / coupon");

        return "admin/promotions/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute("promotion") Promotion promotion,
                       BindingResult result,
                       HttpSession session,
                       Model model) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        validatePromotion(promotion, result);

        if (result.hasErrors()) {
            model.addAttribute("pageTitle", promotion.getId() == null
                    ? "Thêm khuyến mãi / coupon"
                    : "Cập nhật khuyến mãi / coupon");
            return "admin/promotions/form";
        }

        try {
            promotionService.save(promotion);
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("pageTitle", promotion.getId() == null
                    ? "Thêm khuyến mãi / coupon"
                    : "Cập nhật khuyến mãi / coupon");
            return "admin/promotions/form";
        }

        return "redirect:/admin/promotions";
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         HttpSession session) {
        if (!isAdmin(session)) {
            return "redirect:/login";
        }

        promotionService.delete(id);
        return "redirect:/admin/promotions";
    }

    private void validatePromotion(Promotion promotion, BindingResult result) {
        if (promotion.getDiscountPercent() == null) {
            promotion.setDiscountPercent(0.0);
        }

        if (promotion.getDiscountPercent() < 0 || promotion.getDiscountPercent() > 100) {
            result.rejectValue("discountPercent", "discountPercent.invalid",
                    "Phần trăm giảm giá sản phẩm phải từ 0 đến 100");
        }

        if (promotion.getCouponCode() != null) {
            promotion.setCouponCode(promotion.getCouponCode().trim().toUpperCase());

            if (promotion.getCouponCode().isBlank()) {
                promotion.setCouponCode(null);
            }
        }

        if (promotion.getCouponCode() != null && !promotion.getCouponCode().isBlank()) {
            if (promotion.getCouponValue() == null || promotion.getCouponValue() <= 0) {
                result.rejectValue("couponValue", "couponValue.invalid",
                        "Giá trị coupon phải lớn hơn 0");
            }

            if (Promotion.DISCOUNT_PERCENT.equals(promotion.getDiscountType())) {
                if (promotion.getCouponValue() != null && promotion.getCouponValue() > 100) {
                    result.rejectValue("couponValue", "couponValue.percent.invalid",
                            "Coupon giảm theo phần trăm không được vượt quá 100%");
                }
            }

            if (!Promotion.DISCOUNT_PERCENT.equals(promotion.getDiscountType())
                    && !Promotion.DISCOUNT_AMOUNT.equals(promotion.getDiscountType())) {
                result.rejectValue("discountType", "discountType.invalid",
                        "Loại giảm giá không hợp lệ");
            }

            if (promotion.getMinOrderAmount() == null) {
                promotion.setMinOrderAmount(0.0);
            }

            if (promotion.getMinOrderAmount() < 0) {
                result.rejectValue("minOrderAmount", "minOrderAmount.invalid",
                        "Điều kiện đơn tối thiểu không được âm");
            }

            if (promotion.getUsageLimit() != null && promotion.getUsageLimit() < 0) {
                result.rejectValue("usageLimit", "usageLimit.invalid",
                        "Số lượt sử dụng không được âm");
            }

            if (promotion.getUsedCount() == null) {
                promotion.setUsedCount(0);
            }
        }

        if (promotion.getStartDate() != null && promotion.getEndDate() != null
                && promotion.getEndDate().isBefore(promotion.getStartDate())) {
            result.rejectValue("endDate", "endDate.invalid",
                    "Ngày kết thúc phải sau ngày bắt đầu");
        }
    }

    private boolean isAdmin(HttpSession session) {
        AppUser user = (AppUser) session.getAttribute("currentUser");
        return user != null && "ADMIN".equals(user.getRole());
    }
}