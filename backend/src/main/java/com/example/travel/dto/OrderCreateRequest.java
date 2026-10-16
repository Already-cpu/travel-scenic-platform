package com.example.travel.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

public class OrderCreateRequest {
    @NotNull(message = "景点编号不能为空")
    private Long scenicId;
    @NotNull(message = "游览日期不能为空")
    private LocalDate visitDate;
    private Long ticketId;
    private String ticketType = "成人票";
    @NotNull(message = "购票数量不能为空")
    @Min(value = 1, message = "购票数量至少为 1")
    @javax.validation.constraints.Max(value = 20, message = "单次最多预订 20 张")
    private Integer quantity;
    @NotBlank(message = "联系人姓名不能为空")
    private String contactName;
    @NotBlank(message = "联系人手机号不能为空")
    @javax.validation.constraints.Pattern(regexp = "^1[3-9][0-9]{9}$", message = "联系人手机号格式不正确")
    private String contactPhone;

    public Long getScenicId() { return scenicId; }
    public String getTicketType() { return ticketType; }
    public void setTicketType(String ticketType) { this.ticketType = ticketType; }
    public Long getTicketId() { return ticketId; }
    public void setTicketId(Long ticketId) { this.ticketId = ticketId; }
    public void setScenicId(Long scenicId) { this.scenicId = scenicId; }
    public LocalDate getVisitDate() { return visitDate; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getContactName() { return contactName; }
    public void setContactName(String contactName) { this.contactName = contactName; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
}
