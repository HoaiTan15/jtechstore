package com.jtech.jtechstore.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên đăng nhập không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)", unique = true)
    private String username;

    @NotBlank(message = "Mật khẩu không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String password;

    @NotBlank(message = "Họ tên không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Column(columnDefinition = "NVARCHAR(255)")
    private String phone;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    @Column(columnDefinition = "NVARCHAR(255)", unique = true)
    private String email;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String role = "USER";

    @Column(columnDefinition = "NVARCHAR(255)")
    private String provinceName;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String wardName;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String addressDetail;

    @Column(columnDefinition = "NVARCHAR(20)")
    private String resetCode;

    private LocalDateTime resetCodeExpireAt;

    private Boolean enabled = true;
}