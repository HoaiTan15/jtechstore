package com.jtech.jtechstore.service;

import com.jtech.jtechstore.model.AppUser;
import com.jtech.jtechstore.repository.AppUserRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;

    public AuthService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser register(AppUser user) {
        String username = user.getUsername() != null ? user.getUsername().trim() : "";
        String email = user.getEmail() != null ? user.getEmail().trim().toLowerCase() : "";
        String password = user.getPassword() != null ? user.getPassword().trim() : "";
        String fullName = user.getFullName() != null ? user.getFullName().trim() : "";
        String phone = user.getPhone() != null ? user.getPhone().trim() : "";

        if (username.isBlank()) {
            throw new RuntimeException("Tên đăng nhập không được để trống");
        }

        if (password.isBlank()) {
            throw new RuntimeException("Mật khẩu không được để trống");
        }

        if (password.length() < 6) {
            throw new RuntimeException("Mật khẩu phải có ít nhất 6 ký tự");
        }

        if (fullName.isBlank()) {
            throw new RuntimeException("Họ tên không được để trống");
        }

        if (phone.isBlank()) {
            throw new RuntimeException("Số điện thoại không được để trống");
        }

        if (!phone.matches("\\d{9,11}")) {
            throw new RuntimeException("Số điện thoại phải là số và có từ 9 đến 11 chữ số");
        }

        if (email.isBlank()) {
            throw new RuntimeException("Email không được để trống");
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new RuntimeException("Email không đúng định dạng");
        }

        if (appUserRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        if (appUserRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setRole("USER");
        user.setEnabled(true);

        return appUserRepository.save(user);
    }

    public AppUser login(String username, String password) {
        String cleanUsername = username != null ? username.trim() : "";
        String cleanPassword = password != null ? password.trim() : "";

        AppUser user = appUserRepository.findByUsername(cleanUsername)
                .orElseThrow(() -> new RuntimeException("Sai tên đăng nhập hoặc mật khẩu"));

        if (!user.getPassword().equals(cleanPassword)) {
            throw new RuntimeException("Sai tên đăng nhập hoặc mật khẩu");
        }

        if (user.getEnabled() != null && !user.getEnabled()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        return user;
    }
}