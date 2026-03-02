package com.nanguayunpan.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * 服务层工具类
 */
@Component
public class ServiceUtils {

    /**
     * 从请求中获取用户ID
     */
    public static String getUserIdFromRequest(HttpServletRequest request) {
        // 1. 优先从JWT认证过滤器设置的属性中获取
        String userId = (String) request.getAttribute("userId");
        if (StringUtils.isNotEmpty(userId)) {
            return userId;
        }
        
        // 2. 从session中获取（兼容原有逻辑）
        userId = (String) request.getSession().getAttribute("userId");
        if (StringUtils.isNotEmpty(userId)) {
            return userId;
        }
        
        // 3. 尝试从header中获取
        userId = request.getHeader("X-User-Id");
        
        return userId;
    }

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isEmpty(email)) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    /**
     * 验证密码强度
     */
    public static boolean isValidPassword(String password) {
        if (StringUtils.isEmpty(password) || password.length() < 6) {
            return false;
        }
        // 密码至少包含字母和数字
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        return hasLetter && hasDigit;
    }

    /**
     * 格式化文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    /**
     * 获取文件扩展名
     */
    public static String getFileExtension(String fileName) {
        if (StringUtils.isEmpty(fileName)) {
            return "";
        }
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            return fileName.substring(dotIndex + 1).toLowerCase();
        }
        return "";
    }

    /**
     * 验证文件类型是否允许上传
     */
    public static boolean isAllowedFileType(String fileName) {
        String extension = getFileExtension(fileName);
        // 定义允许的文件类型
        String[] allowedTypes = {"txt", "pdf", "doc", "docx", "xls", "xlsx",
                "ppt", "pptx", "jpg", "jpeg", "png", "gif",
                "mp3", "mp4", "zip", "rar"};

        for (String type : allowedTypes) {
            if (type.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成安全的文件名
     */
    public static String generateSafeFileName(String originalFileName) {
        if (StringUtils.isEmpty(originalFileName)) {
            return "file_" + System.currentTimeMillis();
        }

        // 移除路径信息，只保留文件名
        String fileName = originalFileName;
        int lastSeparator = Math.max(originalFileName.lastIndexOf('/'), originalFileName.lastIndexOf('\\'));
        if (lastSeparator > 0) {
            fileName = originalFileName.substring(lastSeparator + 1);
        }

        // 移除特殊字符
        fileName = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");

        return fileName;
    }
}