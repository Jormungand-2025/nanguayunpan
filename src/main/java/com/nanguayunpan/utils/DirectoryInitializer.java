package com.nanguayunpan.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 目录初始化工具类
 * 确保应用启动时文件存储目录结构正确
 */
@Component
public class DirectoryInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryInitializer.class);

    @Value("${project.folder}")
    private String projectFolder;

    /**
     * 应用启动时初始化目录结构
     */
    @PostConstruct
    public void initDirectories() {
        try {
            // 检查主目录是否存在
            File mainDir = new File(projectFolder);
            if (!mainDir.exists()) {
                if (mainDir.mkdirs()) {
                    logger.info("创建主目录: {}", projectFolder);
                } else {
                    logger.error("创建主目录失败: {}", projectFolder);
                    return;
                }
            }

            // 检查目录权限
            if (!mainDir.canWrite()) {
                logger.error("目录没有写入权限: {}", projectFolder);
                return;
            }

            // 创建必要的子目录
            String[] subDirs = {
                    "files",      // 文件存储目录
                    "logs",       // 日志文件目录
                    "temp",       // 临时文件目录
                    "backup"      // 备份文件目录
            };

            for (String subDir : subDirs) {
                File dir = new File(projectFolder + File.separator + subDir);
                if (!dir.exists()) {
                    if (dir.mkdirs()) {
                        logger.info("创建子目录: {}", dir.getAbsolutePath());
                    } else {
                        logger.error("创建子目录失败: {}", dir.getAbsolutePath());
                    }
                }
            }

            logger.info("目录结构初始化完成: {}", projectFolder);

        } catch (Exception e) {
            logger.error("目录初始化失败", e);
        }
    }

    /**
     * 检查目录是否可写
     */
    public boolean isDirectoryWritable() {
        try {
            File testFile = new File(projectFolder + File.separator + "test.tmp");
            if (testFile.createNewFile()) {
                testFile.delete();
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("检查目录写入权限失败", e);
            return false;
        }
    }

    /**
     * 获取目录使用情况
     */
    public String getDirectoryInfo() {
        File dir = new File(projectFolder);
        if (!dir.exists()) {
            return "目录不存在: " + projectFolder;
        }

        long totalSpace = dir.getTotalSpace();
        long freeSpace = dir.getFreeSpace();
        long usedSpace = totalSpace - freeSpace;

        return String.format("目录: %s, 总空间: %.2f GB, 已用: %.2f GB, 可用: %.2f GB",
                projectFolder,
                totalSpace / (1024.0 * 1024 * 1024),
                usedSpace / (1024.0 * 1024 * 1024),
                freeSpace / (1024.0 * 1024 * 1024));
    }
}