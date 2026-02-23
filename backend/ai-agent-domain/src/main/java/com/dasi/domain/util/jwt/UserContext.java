package com.dasi.domain.util.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class UserContext {

    private final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    public void set(UserInfo userInfo) {
        threadLocal.set(userInfo);
    }

    public UserInfo getUser() {
        return threadLocal.get();
    }

    public String getUserName() {
        UserInfo userInfo = threadLocal.get();
        return userInfo == null ? null : userInfo.getUserName();
    }

    public Long getUserId() {
        UserInfo userInfo = threadLocal.get();
        return userInfo == null ? null : userInfo.getUserId();
    }

    public String getUserRole() {
        UserInfo userInfo = threadLocal.get();
        return userInfo == null ? null : userInfo.getUserRole();
    }

    public void clear() {
        threadLocal.remove();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long userId;
        private String userName;
        private String userRole;
    }
}
