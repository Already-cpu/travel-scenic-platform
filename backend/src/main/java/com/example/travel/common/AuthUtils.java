package com.example.travel.common;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

public final class AuthUtils {
    private AuthUtils() {
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && authentication.getPrincipal() instanceof String;
    }

    public static String currentSubject() {
        if (!isAuthenticated()) {
            throw new BusinessException(401, "请先登录");
        }
        return String.valueOf(SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    public static Long currentId() {
        String subject = currentSubject();
        int separator = subject.indexOf(':');
        return Long.valueOf(separator > 0 ? subject.substring(separator + 1) : subject);
    }

    public static boolean isAdmin() {
        return currentSubject().startsWith("ADMIN:");
    }

    public static void requireAdmin() {
        if (!isAdmin()) {
            throw new BusinessException(401, "需要管理员权限");
        }
    }

    public static void requireUser() {
        if (isAdmin()) {
            throw new BusinessException(401, "该接口仅限用户账号使用");
        }
    }
}
