package com.example.travel.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import javax.validation.Valid;
import javax.validation.constraints.Size;

public class ScenicRequest {
    private Long id;
    @NotBlank(message = "景点名称不能为空")
    private String name;
    @NotBlank(message = "省份不能为空")
    private String province;
    @NotBlank(message = "城市不能为空")
    private String city;
    @NotBlank(message = "地址不能为空")
    private String address;
    @NotBlank(message = "景点类型不能为空")
    private String type;
    @NotNull(message = "门票价格不能为空")
    @DecimalMin(value = "0", message = "门票价格不能小于 0")
    private BigDecimal price;
    @NotBlank(message = "景点介绍不能为空")
    private String intro;
    private String image;
    private String openTime;
    private Integer status;
    @Valid
    @Size(min = 1, max = 8, message = "请至少配置 1 个票种，最多 8 个")
    private List<TicketRequest> tickets;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public String getIntro() { return intro; }
    public void setIntro(String intro) { this.intro = intro; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getOpenTime() { return openTime; }
    public void setOpenTime(String openTime) { this.openTime = openTime; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public List<TicketRequest> getTickets() { return tickets; }
    public void setTickets(List<TicketRequest> tickets) { this.tickets = tickets; }
}
