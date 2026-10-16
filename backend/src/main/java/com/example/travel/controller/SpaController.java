package com.example.travel.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 让打包进 Spring Boot 的 Vue history 路由在刷新时仍返回首页。
 * API、上传文件和静态资源不经过这里。
 */
@Controller
public class SpaController {
    @GetMapping({
            "/", "/login", "/register", "/scenic", "/scenic/{id}", "/account",
            "/admin", "/admin/login", "/admin/dashboard", "/admin/scenic",
            "/admin/order", "/admin/user", "/admin/comment"
    })
    public String index() {
        return "forward:/index.html";
    }
}
