package com.example.travel.dto;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;

public class CommentReplyRequest {
    @NotNull(message = "评价编号不能为空")
    private Long id;
    @NotBlank(message = "回复内容不能为空")
    @javax.validation.constraints.Size(max = 1000, message = "回复最多 1000 字")
    private String reply;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReply() { return reply; }
    public void setReply(String reply) { this.reply = reply; }
}
