package com.nanguayunpan.service.impl;

import com.nanguayunpan.entity.FileInfo;
import com.nanguayunpan.entity.ShareInfo;
import com.nanguayunpan.entity.UserInfo;
import com.nanguayunpan.entity.vo.ResponseVO;
import com.nanguayunpan.mapper.FileMapper;
import com.nanguayunpan.mapper.ShareMapper;
import com.nanguayunpan.mapper.UserMapper;
import com.nanguayunpan.service.ShareService;
import com.nanguayunpan.utils.ServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 分享服务实现类
 */
@Service("shareService")
public class ShareServiceImpl implements ShareService {

    private static final Logger logger = LoggerFactory.getLogger(ShareServiceImpl.class);

    @Resource
    private ShareMapper shareMapper;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private UserMapper userMapper;

    @Value("${project.folder}")
    private String projectFolder;

    @Value("${server.servlet.context-path:/}")
    private String contextPath;

    @Value("${server.port:7090}")
    private String serverPort;

    @Override
    @Transactional
    public ResponseVO createShare(String fileId, Integer shareType, Integer expireDays, String password, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(fileId)) {
                return ResponseVO.error("文件ID不能为空");
            }

            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 检查文件是否存在且属于当前用户
            FileInfo fileInfo = fileMapper.selectById(fileId);
            if (fileInfo == null || !fileInfo.getUserId().equals(userId)) {
                return ResponseVO.error("文件不存在或无权分享");
            }

            // 检查文件状态
            if (fileInfo.getStatus() != 1) {
                return ResponseVO.error("文件不可用");
            }

            // 生成分享信息
            ShareInfo shareInfo = new ShareInfo();
            shareInfo.setShareId(generateShareId());
            shareInfo.setFileId(fileId);
            shareInfo.setUserId(userId);
            shareInfo.setShareCode(generateShareCode());
            shareInfo.setCreateTime(new Date());

            // 设置过期时间
            if (expireDays != null && expireDays > 0) {
                long expireTime = System.currentTimeMillis() + expireDays * 24 * 60 * 60 * 1000L;
                shareInfo.setExpireTime(new Date(expireTime));
            } else {
                // 默认7天过期
                long expireTime = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L;
                shareInfo.setExpireTime(new Date(expireTime));
            }

            // 设置分享类型和密码
            shareInfo.setShareType(shareType != null ? shareType : 0);
            if (shareInfo.getShareType() == 1 && StringUtils.isNotEmpty(password)) {
                shareInfo.setPassword(password);
            }

            shareInfo.setViewCount(0);
            shareInfo.setDownloadCount(0);
            shareInfo.setStatus(1);

            // 生成分享链接
            String shareUrl = generateShareUrl(shareInfo.getShareCode());
            shareInfo.setShareUrl(shareUrl);

            // 保存分享信息
            shareMapper.insert(shareInfo);

            logger.info("文件分享创建成功: {} -> {}", fileId, shareInfo.getShareCode());

            // 返回分享信息（排除敏感信息）
            Map<String, Object> result = new HashMap<>();
            result.put("shareCode", shareInfo.getShareCode());
            result.put("shareUrl", shareInfo.getShareUrl());
            result.put("expireTime", shareInfo.getExpireTime());
            result.put("shareType", shareInfo.getShareType());

            return ResponseVO.success(result);
        } catch (Exception e) {
            logger.error("创建文件分享失败: {}", fileId, e);
            return ResponseVO.error("创建分享失败");
        }
    }

    @Override
    public ResponseVO getShareInfo(String shareCode) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(shareCode)) {
                return ResponseVO.error("分享码不能为空");
            }

            // 查询分享信息
            ShareInfo shareInfo = shareMapper.selectByShareCode(shareCode);
            if (shareInfo == null) {
                return ResponseVO.error("分享不存在");
            }

            // 检查分享是否有效
            if (!isShareValid(shareInfo)) {
                return ResponseVO.error("分享已失效");
            }

            // 查询文件信息
            FileInfo fileInfo = fileMapper.selectById(shareInfo.getFileId());
            if (fileInfo == null || fileInfo.getStatus() != 1) {
                return ResponseVO.error("文件不存在或已删除");
            }

            // 更新查看次数
            shareInfo.setViewCount(shareInfo.getViewCount() + 1);
            shareMapper.updateById(shareInfo);

            // 构建返回结果（排除敏感信息）
            Map<String, Object> result = new HashMap<>();
            result.put("shareCode", shareInfo.getShareCode());
            result.put("fileName", fileInfo.getFileName());
            result.put("fileSize", fileInfo.getFileSize());
            result.put("createTime", fileInfo.getCreateTime());
            result.put("shareType", shareInfo.getShareType());
            result.put("expireTime", shareInfo.getExpireTime());
            result.put("viewCount", shareInfo.getViewCount());
            result.put("downloadCount", shareInfo.getDownloadCount());

            // 如果是私密分享，不返回文件详细信息
            if (shareInfo.getShareType() == 1) {
                result.put("needPassword", true);
                result.remove("fileName");
                result.remove("fileSize");
            }

            return ResponseVO.success(result);
        } catch (Exception e) {
            logger.error("获取分享信息失败: {}", shareCode, e);
            return ResponseVO.error("获取分享信息失败");
        }
    }

    @Override
    public ResponseVO validateSharePassword(String shareCode, String password) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(shareCode) || StringUtils.isEmpty(password)) {
                return ResponseVO.error("参数不能为空");
            }

            // 查询分享信息
            ShareInfo shareInfo = shareMapper.selectByShareCode(shareCode);
            if (shareInfo == null) {
                return ResponseVO.error("分享不存在");
            }

            // 检查分享是否有效
            if (!isShareValid(shareInfo)) {
                return ResponseVO.error("分享已失效");
            }

            // 验证密码
            if (shareInfo.getShareType() == 1 && !password.equals(shareInfo.getPassword())) {
                return ResponseVO.error("密码错误");
            }

            // 查询文件信息
            FileInfo fileInfo = fileMapper.selectById(shareInfo.getFileId());
            if (fileInfo == null || fileInfo.getStatus() != 1) {
                return ResponseVO.error("文件不存在或已删除");
            }

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("fileName", fileInfo.getFileName());
            result.put("fileSize", fileInfo.getFileSize());
            result.put("createTime", fileInfo.getCreateTime());
            result.put("fileId", fileInfo.getFileId());

            return ResponseVO.success(result);
        } catch (Exception e) {
            logger.error("验证分享密码失败: {}", shareCode, e);
            return ResponseVO.error("验证失败");
        }
    }

    @Override
    public ResponseVO getShareFileList(String shareCode, String filePid) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(shareCode)) {
                return ResponseVO.error("分享码不能为空");
            }

            // 查询分享信息
            ShareInfo shareInfo = shareMapper.selectByShareCode(shareCode);
            if (shareInfo == null) {
                return ResponseVO.error("分享不存在");
            }

            // 检查分享是否有效
            if (!isShareValid(shareInfo)) {
                return ResponseVO.error("分享已失效");
            }

            // 如果是文件夹分享，获取文件列表
            FileInfo sharedFile = fileMapper.selectById(shareInfo.getFileId());
            if (sharedFile == null || sharedFile.getStatus() != 1) {
                return ResponseVO.error("文件不存在或已删除");
            }

            List<FileInfo> fileList;
            if (sharedFile.getFolderType() == 1) {
                // 文件夹分享
                fileList = fileMapper.selectByPid(sharedFile.getFileId(), 0, 100);
            } else {
                // 文件分享
                fileList = Arrays.asList(sharedFile);
            }

            return ResponseVO.success(fileList);
        } catch (Exception e) {
            logger.error("获取分享文件列表失败: {}", shareCode, e);
            return ResponseVO.error("获取文件列表失败");
        }
    }

    @Override
    public void downloadShareFile(String shareCode, String fileId, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(shareCode) || StringUtils.isEmpty(fileId)) {
                return;
            }

            // 查询分享信息
            ShareInfo shareInfo = shareMapper.selectByShareCode(shareCode);
            if (shareInfo == null) {
                return;
            }

            // 检查分享是否有效
            if (!isShareValid(shareInfo)) {
                return;
            }

            // 查询文件信息
            FileInfo fileInfo = fileMapper.selectById(fileId);
            if (fileInfo == null || fileInfo.getStatus() != 1) {
                return;
            }

            // 更新下载次数
            shareInfo.setDownloadCount(shareInfo.getDownloadCount() + 1);
            shareMapper.updateById(shareInfo);

            // 设置响应头
            HttpServletResponse response = (HttpServletResponse) request.getAttribute("response");
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition",
                    "attachment; filename=" + URLEncoder.encode(fileInfo.getFileName(), "UTF-8"));

            // 读取文件并写入响应流
            File file = new File(projectFolder + fileInfo.getFilePath());
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }

            logger.info("分享文件下载成功: {} -> {}", shareCode, fileId);
        } catch (Exception e) {
            logger.error("分享文件下载失败: {} -> {}", shareCode, fileId, e);
        }
    }

    @Override
    public ResponseVO cancelShare(String shareCode, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(shareCode)) {
                return ResponseVO.error("分享码不能为空");
            }

            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 查询分享信息
            ShareInfo shareInfo = shareMapper.selectByShareCode(shareCode);
            if (shareInfo == null || !shareInfo.getUserId().equals(userId)) {
                return ResponseVO.error("分享不存在或无权操作");
            }

            // 取消分享（标记为失效）
            shareInfo.setStatus(0);
            shareMapper.updateById(shareInfo);

            logger.info("分享取消成功: {}", shareCode);
            return ResponseVO.success("分享已取消");
        } catch (Exception e) {
            logger.error("取消分享失败: {}", shareCode, e);
            return ResponseVO.error("取消分享失败");
        }
    }

    @Override
    public ResponseVO getMyShareList(HttpServletRequest request) {
        try {
            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 查询用户的分享列表
            List<ShareInfo> shareList = shareMapper.selectByUserId(userId);

            // 构建返回结果
            List<Map<String, Object>> result = new ArrayList<>();
            for (ShareInfo share : shareList) {
                Map<String, Object> map = new HashMap<>();
                map.put("shareCode", share.getShareCode());
                map.put("shareUrl", share.getShareUrl());
                map.put("createTime", share.getCreateTime());
                map.put("expireTime", share.getExpireTime());
                map.put("shareType", share.getShareType());
                map.put("viewCount", share.getViewCount());
                map.put("downloadCount", share.getDownloadCount());
                map.put("status", share.getStatus());
                
                // 查询文件信息
                FileInfo fileInfo = fileMapper.selectById(share.getFileId());
                if (fileInfo != null) {
                    map.put("fileName", fileInfo.getFileName());
                    map.put("fileSize", fileInfo.getFileSize());
                }
                
                result.add(map);
            }

            return ResponseVO.success(result);
        } catch (Exception e) {
            logger.error("获取我的分享列表失败", e);
            return ResponseVO.error("获取分享列表失败");
        }
    }

    @Override
    public ResponseVO checkShareValid(String shareCode) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(shareCode)) {
                return ResponseVO.error("分享码不能为空");
            }

            // 查询分享信息
            ShareInfo shareInfo = shareMapper.selectByShareCode(shareCode);
            if (shareInfo == null) {
                return ResponseVO.error("分享不存在");
            }

            // 检查分享是否有效
            boolean valid = isShareValid(shareInfo);

            Map<String, Object> result = new HashMap<>();
            result.put("valid", valid);
            result.put("shareType", shareInfo.getShareType());

            if (!valid) {
                result.put("message", "分享已失效");
            }

            return ResponseVO.success(result);
        } catch (Exception e) {
            logger.error("检查分享有效性失败: {}", shareCode, e);
            return ResponseVO.error("检查失败");
        }
    }

    @Override
    public ResponseVO updateShareSettings(String shareCode, Integer expireDays, String password, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(shareCode)) {
                return ResponseVO.error("分享码不能为空");
            }

            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 查询分享信息
            ShareInfo shareInfo = shareMapper.selectByShareCode(shareCode);
            if (shareInfo == null || !shareInfo.getUserId().equals(userId)) {
                return ResponseVO.error("分享不存在或无权操作");
            }

            // 更新过期时间
            if (expireDays != null && expireDays > 0) {
                long expireTime = System.currentTimeMillis() + expireDays * 24 * 60 * 60 * 1000L;
                shareInfo.setExpireTime(new Date(expireTime));
            }

            // 更新密码
            if (StringUtils.isNotEmpty(password)) {
                shareInfo.setPassword(password);
                shareInfo.setShareType(1); // 设置为私密分享
            }

            shareMapper.updateById(shareInfo);

            logger.info("分享设置更新成功: {}", shareCode);
            return ResponseVO.success("设置更新成功");
        } catch (Exception e) {
            logger.error("更新分享设置失败: {}", shareCode, e);
            return ResponseVO.error("更新失败");
        }
    }

    @Override
    public ResponseVO getShareStatistics(String shareCode, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(shareCode)) {
                return ResponseVO.error("分享码不能为空");
            }

            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 查询分享信息
            ShareInfo shareInfo = shareMapper.selectByShareCode(shareCode);
            if (shareInfo == null || !shareInfo.getUserId().equals(userId)) {
                return ResponseVO.error("分享不存在或无权查看");
            }

            // 构建统计信息
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("viewCount", shareInfo.getViewCount());
            statistics.put("downloadCount", shareInfo.getDownloadCount());
            statistics.put("createTime", shareInfo.getCreateTime());
            statistics.put("expireTime", shareInfo.getExpireTime());
            statistics.put("shareType", shareInfo.getShareType());
            statistics.put("status", shareInfo.getStatus());

            return ResponseVO.success(statistics);
        } catch (Exception e) {
            logger.error("获取分享统计失败: {}", shareCode, e);
            return ResponseVO.error("获取统计失败");
        }
    }

    /**
     * 检查分享是否有效
     */
    private boolean isShareValid(ShareInfo shareInfo) {
        if (shareInfo.getStatus() != 1) {
            return false;
        }

        // 检查过期时间
        if (shareInfo.getExpireTime() != null &&
                shareInfo.getExpireTime().before(new Date())) {
            return false;
        }

        return true;
    }

    /**
     * 生成分享ID
     */
    private String generateShareId() {
        return "S" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    /**
     * 生成分享码
     */
    private String generateShareCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        return code.toString();
    }

    /**
     * 生成分享链接
     */
    private String generateShareUrl(String shareCode) {
        return "http://localhost:" + serverPort + contextPath + "/share/" + shareCode;
    }
}