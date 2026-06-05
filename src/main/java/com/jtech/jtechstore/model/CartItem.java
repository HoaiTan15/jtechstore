package com.jtech.jtechstore.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Product product;
    private int quantity;

    public double getTotalPrice() {
        return product.getSalePrice() * quantity;
    }
}