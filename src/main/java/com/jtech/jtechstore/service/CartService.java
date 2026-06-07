package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.CartItem;
import com.jtech.jtechstore.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {
    private final List<CartItem> cartItems = new ArrayList<>();

    public List<CartItem> getCartItems() {
        return cartItems;
    }

    public void addToCart(Product product) {
        if (product == null || product.getId() == null) {
            throw new RuntimeException("Sản phẩm không hợp lệ");
        }

        if (product.getQuantity() == null || product.getQuantity() <= 0) {
            throw new RuntimeException("Sản phẩm đã hết hàng");
        }

        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(product.getId())) {
                int newQuantity = item.getQuantity() + 1;

                if (newQuantity > product.getQuantity()) {
                    throw new RuntimeException("Số lượng trong giỏ hàng vượt quá số lượng tồn kho");
                }

                item.setQuantity(newQuantity);
                return;
            }
        }

        cartItems.add(new CartItem(product, 1));
    }

    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            removeFromCart(productId);
            return;
        }

        for (CartItem item : cartItems) {
            if (item.getProduct().getId().equals(productId)) {
                Product product = item.getProduct();

                if (product.getQuantity() == null || product.getQuantity() <= 0) {
                    throw new RuntimeException("Sản phẩm đã hết hàng");
                }

                if (quantity > product.getQuantity()) {
                    throw new RuntimeException("Số lượng yêu cầu vượt quá số lượng tồn kho");
                }

                item.setQuantity(quantity);
                return;
            }
        }

        throw new RuntimeException("Không tìm thấy sản phẩm trong giỏ hàng");
    }

    public void removeFromCart(Long productId) {
        cartItems.removeIf(item -> item.getProduct().getId().equals(productId));
    }

    public void clearCart() {
        cartItems.clear();
    }

    public boolean isEmpty() {
        return cartItems.isEmpty();
    }

    public double getTotal() {
        return cartItems.stream()
                .mapToDouble(CartItem::getTotalPrice)
                .sum();
    }
}