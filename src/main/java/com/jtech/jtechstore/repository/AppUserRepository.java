package com.jtech.jtechstore.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.jtech.jtechstore.model.AppUser;
import java.util.List;
import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);

    List<AppUser> findByRoleOrderByIdDesc(String role);

    List<AppUser> findAllByOrderByIdDesc();
}