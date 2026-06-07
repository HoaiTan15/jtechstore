package com.jtech.jtechstore.repository;

import com.jtech.jtechstore.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    List<AppUser> findByRoleOrderByIdDesc(String role);

    List<AppUser> findAllByOrderByIdDesc();
}