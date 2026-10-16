package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import com.example.travel.dto.UserStatusRequest;
import com.example.travel.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {
    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public ApiResponse<?> list(@RequestParam(defaultValue = "1") long current,
                               @RequestParam(defaultValue = "12") long size,
                               @RequestParam(required = false) String keyword) {
        AuthUtils.requireAdmin();
        return ApiResponse.success(userService.adminPage(current, size, keyword));
    }

    @PutMapping("/status")
    public ApiResponse<Void> status(@Valid @RequestBody UserStatusRequest request) {
        AuthUtils.requireAdmin();
        userService.updateStatus(request.getId(), request.getStatus());
        return ApiResponse.success("用户状态已更新", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthUtils.requireAdmin();
        userService.delete(id);
        return ApiResponse.success("用户已删除", null);
    }
}
