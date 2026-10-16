package com.example.travel.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class OrderStatusRequest {
    @NotNull(message = "订单编号不能为空")
    private Long id;
    @NotBlank(message = "订单状态不能为空")
    private String status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
