package com.example.travel.dto;

import com.example.travel.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class UserView {
    private Long id;
    private String phone;
    private String nickname;
    private String realName;
    private String avatar;
    private BigDecimal walletBalance;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static UserView from(User user) {
        UserView view = new UserView();
        view.id = user.getId();
        view.phone = user.getPhone();
        view.nickname = user.getNickname();
        view.realName = user.getRealName();
        view.avatar = user.getAvatar();
        view.walletBalance = user.getWalletBalance();
        view.status = user.getStatus();
        view.createTime = user.getCreateTime();
        view.updateTime = user.getUpdateTime();
        return view;
    }

    public Long getId() { return id; }
    public String getPhone() { return phone; }
    public String getNickname() { return nickname; }
    public String getRealName() { return realName; }
    public String getAvatar() { return avatar; }
    public BigDecimal getWalletBalance() { return walletBalance; }
    public Integer getStatus() { return status; }
    public LocalDateTime getCreateTime() { return createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
}
