package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import com.example.travel.service.FavoriteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorite")
public class FavoriteController {
    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping("/{scenicId}")
    public ApiResponse<Void> add(@PathVariable Long scenicId) {
        AuthUtils.requireUser();
        favoriteService.add(scenicId);
        return ApiResponse.success("收藏成功", null);
    }

    @DeleteMapping("/{scenicId}")
    public ApiResponse<Void> remove(@PathVariable Long scenicId) {
        AuthUtils.requireUser();
        favoriteService.remove(scenicId);
        return ApiResponse.success("已取消收藏", null);
    }

    @GetMapping("/my")
    public ApiResponse<?> mine() {
        AuthUtils.requireUser();
        return ApiResponse.success(favoriteService.mine());
    }

    @GetMapping("/check/{scenicId}")
    public ApiResponse<?> check(@PathVariable Long scenicId) {
        AuthUtils.requireUser();
        return ApiResponse.success(favoriteService.check(scenicId));
    }
}
