package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.CartItem;
import com.jtech.jtechstore.model.Order;
import com.jtech.jtechstore.model.OrderDetail;
import com.jtech.jtechstore.repository.OrderRepository;
import com.jtech.jtechstore.model.AppUser;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    public Order checkout(String customerName,
                          String phone,
                          String address,
                          String paymentMethod,
                          AppUser user) {
        Order order = new Order();
        order.setCustomerName(customerName);
        order.setPhone(phone);
        order.setAddress(address);
        order.setOrderDate(LocalDateTime.now());
        order.setTotalAmount(cartService.getTotal());
        order.setPaymentMethod(paymentMethod);
        order.setUser(user);

        if ("BANK_TRANSFER".equals(paymentMethod)) {
            order.setStatus("Chờ thanh toán");
            order.setPaymentStatus("Chưa thanh toán");
        } else {
            order.setStatus("Đang xử lý");
            order.setPaymentStatus("Thanh toán khi nhận hàng");
        }

        List<OrderDetail> details = new ArrayList<>();

        for (CartItem item : cartService.getCartItems()) {
            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(item.getProduct());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getProduct().getSalePrice());
            details.add(detail);
        }

        order.setOrderDetails(details);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();

        return savedOrder;
    }
}