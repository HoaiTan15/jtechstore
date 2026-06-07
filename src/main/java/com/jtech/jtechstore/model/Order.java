package com.jtech.jtechstore.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    public static final String STATUS_PENDING = "Chờ xác nhận";
    public static final String STATUS_CONFIRMED = "Đã xác nhận";
    public static final String STATUS_SHIPPING = "Đang giao";
    public static final String STATUS_COMPLETED = "Hoàn thành";
    public static final String STATUS_CANCELLED = "Đã hủy";

    public static final String PAYMENT_UNPAID = "Chưa thanh toán";
    public static final String PAYMENT_PAID = "Đã thanh toán";
    public static final String PAYMENT_COD = "Thanh toán khi nhận hàng";

    public static final String METHOD_COD = "COD";
    public static final String METHOD_BANK_TRANSFER = "BANK_TRANSFER";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String customerName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String phone;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String address;

    private Double subtotalAmount;

    private Double discountAmount;

    @Column(columnDefinition = "NVARCHAR(100)")
    private String couponCode;

    private Double totalAmount;

    private LocalDateTime orderDate;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String status;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String paymentMethod;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String paymentStatus;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDetail> orderDetails;

    public boolean isPending() {
        return STATUS_PENDING.equals(status);
    }

    public boolean isConfirmed() {
        return STATUS_CONFIRMED.equals(status);
    }

    public boolean isShipping() {
        return STATUS_SHIPPING.equals(status);
    }

    public boolean isCompleted() {
        return STATUS_COMPLETED.equals(status);
    }

    public boolean isCancelled() {
        return STATUS_CANCELLED.equals(status);
    }

    public boolean isPaid() {
        return PAYMENT_PAID.equals(paymentStatus);
    }

    public boolean isBankTransfer() {
        return METHOD_BANK_TRANSFER.equals(paymentMethod);
    }

    public boolean isCod() {
        return METHOD_COD.equals(paymentMethod);
    }

    public boolean canCancel() {
        return STATUS_PENDING.equals(status);
    }
}