package com.example.travel.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AssistantRequest {
    @NotBlank(message = "请输入想咨询的问题")
    @Size(max = 300, message = "问题最多 300 字")
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
