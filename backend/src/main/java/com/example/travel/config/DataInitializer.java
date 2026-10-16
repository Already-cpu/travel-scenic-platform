package com.example.travel.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.travel.entity.Admin;
import com.example.travel.mapper.AdminMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final AdminMapper adminMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminMapper adminMapper, PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminMapper.selectCount(new QueryWrapper<>()) == 0) {
            Admin admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setNickname("平台管理员");
            adminMapper.insert(admin);
        }
    }
}
