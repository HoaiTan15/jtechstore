package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.model.CartItem;
import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.model.OrderDetail;
import com.jtech.jtechstore.model.Product;
import com.jtech.jtechstore.repository.OrderRepository;
import com.jtech.jtechstore.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;
    private final PromotionService promotionService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CartService cartService,
                        PromotionService promotionService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
        this.promotionService = promotionService;
    }

    public Order checkout(String customerName,
                          String phone,
                          String address,
                          String paymentMethod,
                          String couponCode,
                          AppUser user) {
        if (user == null) {
            throw new RuntimeException("Bạn cần đăng nhập để đặt hàng");
        }

        if (cartService.getCartItems().isEmpty()) {
            throw new RuntimeException("Giỏ hàng đang trống");
        }

        if (customerName == null || customerName.trim().isEmpty()) {
            throw new RuntimeException("Họ tên khách hàng không được để trống");
        }

        if (phone == null || phone.trim().isEmpty()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        if (!phone.trim().matches("\\d{9,11}")) {
            throw new RuntimeException("Số điện thoại phải là số và có từ 9 đến 11 chữ số");
        }

        if (address == null || address.trim().isEmpty()) {
            throw new RuntimeException("Địa chỉ giao hàng không được để trống");
        }

        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new RuntimeException("Vui lòng chọn phương thức thanh toán");
        }

        checkStockBeforeCheckout();

        double subtotal = cartService.getTotal();
        double discount = 0;
        String cleanCouponCode = null;

        if (couponCode != null && !couponCode.trim().isEmpty()) {
            cleanCouponCode = couponCode.trim().toUpperCase();
            discount = promotionService.calculateCouponDiscount(cleanCouponCode, subtotal);
        }

        double finalTotal = subtotal - discount;

        if (finalTotal < 0) {
            finalTotal = 0;
        }

        Order order = new Order();
        order.setCustomerName(customerName.trim());
        order.setPhone(phone.trim());
        order.setAddress(address.trim());
        order.setOrderDate(LocalDateTime.now());

        order.setSubtotalAmount(subtotal);
        order.setDiscountAmount(discount);
        order.setCouponCode(cleanCouponCode);
        order.setTotalAmount(finalTotal);

        order.setPaymentMethod(paymentMethod);
        order.setUser(user);
        order.setStatus(Order.STATUS_PENDING);

        if (Order.METHOD_BANK_TRANSFER.equals(paymentMethod)) {
            order.setPaymentStatus(Order.PAYMENT_UNPAID);
        } else {
            order.setPaymentStatus(Order.PAYMENT_COD);
        }

        List<OrderDetail> details = new ArrayList<>();

        for (CartItem item : cartService.getCartItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(product.getSalePrice());

            details.add(detail);
        }

        order.setOrderDetails(details);

        subtractStockAfterCheckout();

        Order savedOrder = orderRepository.save(order);

        if (cleanCouponCode != null) {
            promotionService.increaseCouponUsedCount(cleanCouponCode);
        }

        cartService.clearCart();

        return savedOrder;
    }

    private void checkStockBeforeCheckout() {
        for (CartItem item : cartService.getCartItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong hệ thống"));

            int stock = product.getQuantity() != null ? product.getQuantity() : 0;
            int buyQuantity = item.getQuantity();

            if (stock <= 0) {
                throw new RuntimeException("Sản phẩm \"" + product.getName() + "\" đã hết hàng");
            }

            if (buyQuantity <= 0) {
                throw new RuntimeException("Số lượng sản phẩm \"" + product.getName() + "\" không hợp lệ");
            }

            if (buyQuantity > stock) {
                throw new RuntimeException(
                        "Sản phẩm \"" + product.getName() + "\" chỉ còn " + stock + " sản phẩm trong kho"
                );
            }
        }
    }

    private void subtractStockAfterCheckout() {
        for (CartItem item : cartService.getCartItems()) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm trong hệ thống"));

            int stock = product.getQuantity() != null ? product.getQuantity() : 0;
            int buyQuantity = item.getQuantity();

            if (buyQuantity <= 0) {
                throw new RuntimeException("Số lượng sản phẩm \"" + product.getName() + "\" không hợp lệ");
            }

            if (buyQuantity > stock) {
                throw new RuntimeException(
                        "Sản phẩm \"" + product.getName() + "\" không đủ số lượng trong kho"
                );
            }

            product.setQuantity(stock - buyQuantity);
            productRepository.save(product);
        }
    }
}