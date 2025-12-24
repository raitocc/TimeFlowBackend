package org.whu.timeflow.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.whu.timeflow.utils.JwtUtils;
import org.whu.timeflow.utils.UserContext;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 对于 OPTIONS 请求直接放行 (解决跨域时的预检请求问题)
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 2. 获取 Token (通常放在 Header 的 Authorization 字段，格式 "Bearer xxx")
        String authHeader = request.getHeader("Authorization");

        if (!StringUtils.hasLength(authHeader) || !authHeader.startsWith("Bearer ")) {
            response.setStatus(401); // 无权访问
            return false;
        }

        String token = authHeader.substring(7); // 去掉 "Bearer "

        try {
            // 3. 解析 Token
            Claims claims = JwtUtils.parseToken(token);
            String userId = claims.getSubject();

            // 4. 放入上下文，供后面 Controller 使用
            UserContext.setUserId(userId);

            return true; // 放行
        } catch (Exception e) {
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束，清理线程变量，防止内存泄漏
        UserContext.clear();
    }
}