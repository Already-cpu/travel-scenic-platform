package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import com.example.travel.dto.ScenicRequest;
import com.example.travel.dto.ScenicStatusRequest;
import com.example.travel.service.ScenicService;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/scenic")
public class ScenicController {
    private final ScenicService scenicService;

    public ScenicController(ScenicService scenicService) {
        this.scenicService = scenicService;
    }

    @GetMapping("/list")
    public ApiResponse<?> list(@RequestParam(defaultValue = "1") long current,
                               @RequestParam(defaultValue = "12") long size,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(required = false) String type,
                               @RequestParam(required = false) String province,
                               @RequestParam(required = false) String sort) {
        return ApiResponse.success(scenicService.publicPage(current, size, keyword, type, province, sort));
    }

    @GetMapping("/filters")
    public ApiResponse<?> filters() {
        return ApiResponse.success(scenicService.filters(true));
    }

    @GetMapping("/admin/filters")
    public ApiResponse<?> adminFilters() {
        AuthUtils.requireAdmin();
        return ApiResponse.success(scenicService.filters(false));
    }

    @GetMapping("/hot")
    public ApiResponse<?> hot() {
        return ApiResponse.success(scenicService.hot());
    }

    @GetMapping("/{id}")
    public ApiResponse<?> detail(@PathVariable Long id) {
        return ApiResponse.success(scenicService.detail(id));
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody ScenicRequest request) {
        AuthUtils.requireAdmin();
        return ApiResponse.success("景点已新增", scenicService.create(request));
    }

    @PutMapping
    public ApiResponse<?> update(@Valid @RequestBody ScenicRequest request) {
        AuthUtils.requireAdmin();
        return ApiResponse.success("景点已更新", scenicService.update(request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthUtils.requireAdmin();
        scenicService.delete(id);
        return ApiResponse.success("景点已删除", null);
    }

    @PutMapping("/status")
    public ApiResponse<Void> status(@Valid @RequestBody ScenicStatusRequest request) {
        AuthUtils.requireAdmin();
        scenicService.updateStatus(request.getId(), request.getStatus());
        return ApiResponse.success("景点状态已更新", null);
    }

    @GetMapping("/admin/list")
    public ApiResponse<?> adminList(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "12") long size,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) String type,
                                    @RequestParam(required = false) String province) {
        AuthUtils.requireAdmin();
        return ApiResponse.success(scenicService.adminPage(current, size, keyword, type, province));
    }
}
