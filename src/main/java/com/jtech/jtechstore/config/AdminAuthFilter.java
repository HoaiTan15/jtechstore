package com.jtech.jtechstore.config;

import com.jtech.jtechstore.model.AppUser;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AdminAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.startsWith("/admin")) {
            HttpSession session = request.getSession(false);

            if (session == null) {
                response.sendRedirect("/login");
                return;
            }

            AppUser currentUser = (AppUser) session.getAttribute("currentUser");

            if (currentUser == null) {
                response.sendRedirect("/login");
                return;
            }

            if (!"ADMIN".equals(currentUser.getRole())) {
                response.sendRedirect("/");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}