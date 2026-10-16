package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import com.example.travel.dto.LoginRequest;
import com.example.travel.dto.QrLoginRequest;
import com.example.travel.dto.RegisterRequest;
import com.example.travel.dto.SmsLoginRequest;
import com.example.travel.dto.UserUpdateRequest;
import com.example.travel.service.UserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        userService.register(request);
        return ApiResponse.success("注册成功", null);
    }

    @PostMapping("/login")
    public ApiResponse<Object> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }

    @PostMapping("/sms-login")
    public ApiResponse<Object> smsLogin(@Valid @RequestBody SmsLoginRequest request) {
        return ApiResponse.success(userService.smsLogin(request));
    }

    @PostMapping("/qr-login")
    public ApiResponse<Object> qrLogin(@Valid @RequestBody QrLoginRequest request) {
        return ApiResponse.success(userService.qrLogin(request));
    }

    @GetMapping("/info")
    public ApiResponse<?> info() {
        AuthUtils.requireUser();
        return ApiResponse.success(userService.current());
    }

    @PutMapping("/info")
    public ApiResponse<?> update(@RequestBody UserUpdateRequest request) {
        AuthUtils.requireUser();
        return ApiResponse.success("个人信息已更新", userService.update(request));
    }
}
