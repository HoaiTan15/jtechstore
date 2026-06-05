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
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên khuyến mãi không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String name;

    @Min(value = 0, message = "Phần trăm giảm giá không được âm")
    @Max(value = 100, message = "Phần trăm giảm giá không được vượt quá 100")
    private Double discountPercent;

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
}