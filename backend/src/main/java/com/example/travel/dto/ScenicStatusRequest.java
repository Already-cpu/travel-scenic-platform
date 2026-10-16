package com.example.travel.dto;

import javax.validation.constraints.NotNull;

public class ScenicStatusRequest {
    @NotNull(message = "景点编号不能为空")
    private Long id;
    @NotNull(message = "状态不能为空")
    private Integer status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
