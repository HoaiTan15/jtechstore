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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String customerName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String phone;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String address;

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
}