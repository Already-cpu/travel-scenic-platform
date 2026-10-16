package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import com.example.travel.dto.OrderCreateRequest;
import com.example.travel.dto.OrderStatusRequest;
import com.example.travel.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody OrderCreateRequest request) {
        AuthUtils.requireUser();
        return ApiResponse.success("订单已创建", orderService.create(request));
    }

    @GetMapping("/my")
    public ApiResponse<?> mine() {
        AuthUtils.requireUser();
        return ApiResponse.success(orderService.mine());
    }

    @PutMapping("/cancel/{id}")
    public ApiResponse<Void> cancel(@PathVariable Long id) {
        AuthUtils.requireUser();
        orderService.cancel(id);
        return ApiResponse.success("订单已取消", null);
    }

    @PutMapping("/pay/{id}")
    public ApiResponse<?> pay(@PathVariable Long id, @RequestParam(defaultValue = "wallet") String method) {
        AuthUtils.requireUser();
        return ApiResponse.success("支付成功", orderService.pay(id, method));
    }

    @GetMapping("/list")
    public ApiResponse<?> adminList(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "12") long size,
                                    @RequestParam(required = false) String status,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String startDate,
                                    @RequestParam(required = false) String endDate) {
        AuthUtils.requireAdmin();
        return ApiResponse.success(orderService.adminPage(current, size, status, keyword, startDate, endDate));
    }

    @GetMapping("/recent")
    public ApiResponse<?> recent() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(orderService.recent(5));
    }

    @PutMapping("/status")
    public ApiResponse<Void> status(@Valid @RequestBody OrderStatusRequest request) {
        AuthUtils.requireAdmin();
        orderService.updateStatus(request);
        return ApiResponse.success("订单状态已更新", null);
    }
}
