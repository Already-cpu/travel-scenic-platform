package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.common.AuthUtils;
import com.example.travel.dto.CommentCreateRequest;
import com.example.travel.dto.CommentReplyRequest;
import com.example.travel.dto.CommentStatusRequest;
import com.example.travel.service.CommentService;
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
@RequestMapping("/api/comment")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ApiResponse<?> create(@Valid @RequestBody CommentCreateRequest request) {
        AuthUtils.requireUser();
        return ApiResponse.success("评价已提交", commentService.create(request));
    }

    @GetMapping("/scenic/{scenicId}")
    public ApiResponse<?> scenic(@PathVariable Long scenicId) {
        return ApiResponse.success(commentService.scenicComments(scenicId));
    }

    @GetMapping("/my")
    public ApiResponse<?> mine() {
        AuthUtils.requireUser();
        return ApiResponse.success(commentService.mine());
    }

    @GetMapping("/list")
    public ApiResponse<?> adminList(@RequestParam(required = false) String keyword,
                                    @RequestParam(required = false) Integer status) {
        AuthUtils.requireAdmin();
        return ApiResponse.success(commentService.adminList(keyword, status));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        AuthUtils.requireAdmin();
        commentService.delete(id);
        return ApiResponse.success("评价已删除", null);
    }

    @PutMapping("/reply")
    public ApiResponse<?> reply(@Valid @RequestBody CommentReplyRequest request) {
        AuthUtils.requireAdmin();
        return ApiResponse.success("评价回复已保存", commentService.addReply(request));
    }

    @PutMapping("/status")
    public ApiResponse<Void> status(@Valid @RequestBody CommentStatusRequest request) {
        AuthUtils.requireAdmin();
        commentService.updateStatus(request.getId(), request.getStatus());
        return ApiResponse.success("评价状态已更新", null);
    }
}
