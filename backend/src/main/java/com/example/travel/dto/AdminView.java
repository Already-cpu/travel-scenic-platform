package com.example.travel.dto;

import com.example.travel.entity.Admin;

import java.time.LocalDateTime;

public class AdminView {
    private Long id;
    private String username;
    private String nickname;
    private LocalDateTime createTime;

    public static AdminView from(Admin admin) {
        AdminView view = new AdminView();
        view.id = admin.getId();
        view.username = admin.getUsername();
        view.nickname = admin.getNickname();
        view.createTime = admin.getCreateTime();
        return view;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getNickname() { return nickname; }
    public LocalDateTime getCreateTime() { return createTime; }
}
