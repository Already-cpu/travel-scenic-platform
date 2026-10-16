package com.example.travel.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class TicketRequest {
    private Long id;
    @NotBlank(message = "票种名称不能为空")
    private String name;
    @NotNull(message = "票种价格不能为空")
    @DecimalMin(value = "0", message = "票种价格不能小于 0")
    private BigDecimal price;
    @NotNull(message = "票种库存不能为空")
    @Min(value = 0, message = "票种库存不能小于 0")
    private Integer stock;
    private Integer status = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
