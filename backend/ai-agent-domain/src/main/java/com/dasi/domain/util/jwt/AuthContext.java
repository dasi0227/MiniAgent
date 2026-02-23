package com.dasi.domain.util.jwt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
public class AuthContext {

    private final ThreadLocal<UserInfo> USER_CONTEXT = new ThreadLocal<>();

    public void set(UserInfo userInfo) {
        USER_CONTEXT.set(userInfo);
    }

    public UserInfo getUser() {
        return USER_CONTEXT.get();
    }

    public String getUserName() {
        UserInfo userInfo = USER_CONTEXT.get();
        return userInfo == null ? null : userInfo.getUserName();
    }

    public Long getUserId() {
        UserInfo userInfo = USER_CONTEXT.get();
        return userInfo == null ? null : userInfo.getUserId();
    }

    public String getUserRole() {
        UserInfo userInfo = USER_CONTEXT.get();
        return userInfo == null ? null : userInfo.getUserRole();
    }

    public void clear() {
        USER_CONTEXT.remove();
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
