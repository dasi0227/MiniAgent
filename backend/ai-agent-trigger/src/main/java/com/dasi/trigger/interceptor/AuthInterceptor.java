package com.dasi.trigger.interceptor;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.dasi.domain.util.jwt.UserContext;
import com.dasi.domain.user.model.vo.UserVO;
import com.dasi.domain.util.jwt.IJwtUtil;
import com.dasi.types.result.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.Set;

import static com.dasi.domain.admin.model.enumeration.UserRole.ADMIN;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final Set<String> WHITE_LIST = Set.of(
            "/miniagent/api/v1/user/auth/login",
            "/miniagent/api/v1/user/auth/register"
    );

    @Resource
    private IJwtUtil jwtUtil;

    @Resource
    private UserContext userContext;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        if (WHITE_LIST.stream().anyMatch(uri::startsWith)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(response, "未登录或登录已过期");
        }

        String token = authHeader.substring(7);
        UserVO userVO;

        try {
            userVO = jwtUtil.parseToken(token);
        } catch (Exception e) {
            log.error("【Token 校验】失败", e);
            return unauthorized(response, "登录状态无效，请重新登录");
        }

        userContext.set(UserContext.UserInfo.builder()
                .userId(userVO.getUserId())
                .userName(userVO.getUserName())
                .userRole(userVO.getUserRole())
                .build());

        if (uri.startsWith("/miniagent/api/v1/admin") && (userVO.getUserRole() == null || !ADMIN.getRole().equalsIgnoreCase(userVO.getUserRole()))) {
            return forbidden(response, "无权限访问该资源");
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        userContext.clear();
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws IOException {
        Result<Void> body = Result.error(message);
        writeResponse(response, HttpStatus.UNAUTHORIZED.value(), body);
        return false;
    }

    private boolean forbidden(HttpServletResponse response, String message) throws IOException {
        Result<Void> body = Result.error(message);
        writeResponse(response, HttpStatus.FORBIDDEN.value(), body);
        return false;
    }

    private void writeResponse(HttpServletResponse response, int status, Result<Void> body) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

}
