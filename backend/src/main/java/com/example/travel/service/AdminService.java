package com.example.travel.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.travel.common.BusinessException;
import com.example.travel.dto.AdminLoginRequest;
import com.example.travel.dto.AdminView;
import com.example.travel.dto.TokenResponse;
import com.example.travel.entity.Admin;
import com.example.travel.mapper.AdminMapper;
import com.example.travel.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AdminService {
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AdminService(AdminMapper adminMapper, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public TokenResponse<AdminView> login(AdminLoginRequest request) {
        Admin admin = adminMapper.selectOne(new QueryWrapper<Admin>().eq("username", request.getUsername()));
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPassword())) {
            throw new BusinessException("管理员账号或密码错误");
        }
        return new TokenResponse<>(jwtService.generate("ADMIN:" + admin.getId(), "ADMIN"), AdminView.from(admin));
    }

    public AdminView current(Long id) {
        Admin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(401, "管理员不存在");
        }
        return AdminView.from(admin);
    }
}
