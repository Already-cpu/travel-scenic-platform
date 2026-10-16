package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import com.example.travel.service.StatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/overview")
    public ApiResponse<?> overview() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(statsService.overview());
    }

    @GetMapping("/scenic-views")
    public ApiResponse<?> scenicViews() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(statsService.scenicViews());
    }

    @GetMapping("/order-trend")
    public ApiResponse<?> orderTrend() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(statsService.orderTrend());
    }

    @GetMapping("/region")
    public ApiResponse<?> region() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(statsService.regionDistribution());
    }

    @GetMapping("/hotness")
    public ApiResponse<?> hotness() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(statsService.hotness());
    }

    @GetMapping("/orders")
    public ApiResponse<?> orders(@RequestParam(defaultValue="month") String granularity,
                                @RequestParam(required=false) String startDate,
                                @RequestParam(required=false) String endDate) {
        AuthUtils.requireAdmin();
        return ApiResponse.success(statsService.orderStatistics(granularity,startDate,endDate));
    }

    @GetMapping("/rating")
    public ApiResponse<?> rating() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(statsService.ratingDistribution());
    }
}
