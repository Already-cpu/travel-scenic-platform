package com.example.travel.controller;

import com.example.travel.common.ApiResponse;
import com.example.travel.dto.AssistantRequest;
import com.example.travel.service.AssistantService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/chat")
    public ApiResponse<?> chat(@Valid @RequestBody AssistantRequest request) {
        return ApiResponse.success(assistantService.chat(request.getMessage()));
    }
}
