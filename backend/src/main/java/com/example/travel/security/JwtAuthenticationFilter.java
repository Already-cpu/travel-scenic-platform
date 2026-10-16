package com.example.travel.security;

import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final com.example.travel.mapper.UserMapper userMapper;
    private final com.example.travel.mapper.AdminMapper adminMapper;

    public JwtAuthenticationFilter(JwtService jwtService, com.example.travel.mapper.UserMapper userMapper,
                                   com.example.travel.mapper.AdminMapper adminMapper) {
        this.jwtService = jwtService;
        this.userMapper = userMapper;
        this.adminMapper = adminMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.parse(header.substring(7));
                String subject = claims.getSubject();
                String type = claims.get("type", String.class);
                if (!("USER".equals(type) || "ADMIN".equals(type)) || !subject.startsWith(type + ":")) {
                    throw new IllegalArgumentException("Invalid token type");
                }
                Long id = Long.valueOf(subject.substring(subject.indexOf(':') + 1));
                if ("USER".equals(type)) {
                    com.example.travel.entity.User user = userMapper.selectById(id);
                    if (user == null || !Integer.valueOf(1).equals(user.getStatus())) {
                        throw new IllegalArgumentException("Account disabled or deleted");
                    }
                } else if (adminMapper.selectById(id) == null) {
                    throw new IllegalArgumentException("Admin deleted");
                }
                String role = "ADMIN".equals(type) ? "ROLE_ADMIN" : "ROLE_USER";
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(subject, null,
                                Collections.singletonList(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception ignored) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
