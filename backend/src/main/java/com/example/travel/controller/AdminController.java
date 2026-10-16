package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import com.example.travel.dto.AdminLoginRequest;
import com.example.travel.service.AdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(adminService.login(request));
    }

    @GetMapping("/info")
    public ApiResponse<?> info() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(adminService.current(AuthUtils.currentId()));
    }
}
