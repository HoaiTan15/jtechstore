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
        if (appUserRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Tên đăng nhập đã tồn tại");
        }

        user.setRole("USER");
        user.setEnabled(true);

        return appUserRepository.save(user);
    }

    public AppUser login(String username, String password) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Sai tên đăng nhập hoặc mật khẩu"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Sai tên đăng nhập hoặc mật khẩu");
        }

        if (user.getEnabled() != null && !user.getEnabled()) {
            throw new RuntimeException("Tài khoản đã bị khóa");
        }

        return user;
    }
}