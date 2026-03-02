package com.nanguayunpan.config;

import com.nanguayunpan.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();

        // 放行公开接口
        if (isPublicEndpoint(requestURI)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 获取JWT令牌
        String token = getTokenFromRequest(request);

        if (StringUtils.isEmpty(token)) {
            sendUnauthorizedResponse(response, "缺少JWT令牌");
            return;
        }

        // 验证令牌
        if (!jwtUtil.validateToken(token)) {
            sendUnauthorizedResponse(response, "JWT令牌无效或已过期");
            return;
        }

        // 设置用户信息到请求属性中
        try {
            String userId = jwtUtil.getUserIdFromToken(token);
            String email = jwtUtil.getEmailFromToken(token);

            request.setAttribute("userId", userId);
            request.setAttribute("email", email);

            logger.debug("JWT认证成功 - 用户ID: {}, 邮箱: {}", userId, email);

        } catch (Exception e) {
            logger.error("JWT令牌解析失败: {}", e.getMessage());
            sendUnauthorizedResponse(response, "JWT令牌解析失败");
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从请求中获取JWT令牌
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        // 1. 从Authorization头获取
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.isNotEmpty(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // 2. 从请求参数获取
        String token = request.getParameter("token");
        if (StringUtils.isNotEmpty(token)) {
            return token;
        }

        return null;
    }

    /**
     * 判断是否为公开接口
     */
    private boolean isPublicEndpoint(String requestURI) {
        // 公开接口列表
        return requestURI.startsWith("/api/user/login") ||
                requestURI.startsWith("/api/user/register") ||
                requestURI.startsWith("/api/user/sendEmailCode") ||
                requestURI.startsWith("/api/share/info") ||
                requestURI.startsWith("/api/share/check") ||
                requestURI.startsWith("/api/share/files") ||
                requestURI.startsWith("/api/share/download") ||
                requestURI.startsWith("/api/share/validate");
    }

    /**
     * 发送未授权响应
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");

        String jsonResponse = String.format(
                "{\"status\":\"error\",\"code\":401,\"info\":\"%s\",\"data\":null}",
                message
        );

        response.getWriter().write(jsonResponse);
    }
}