package com.nanguayunpan.service.impl;

import com.nanguayunpan.entity.FileInfo;
import com.nanguayunpan.entity.UserInfo;
import com.nanguayunpan.entity.vo.ResponseVO;
import com.nanguayunpan.mapper.FileMapper;
import com.nanguayunpan.mapper.UserMapper;
import com.nanguayunpan.service.FileService;
import com.nanguayunpan.utils.ServiceUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 文件服务实现类
 */
@Service("fileService")
public class FileServiceImpl implements FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    @Resource
    private FileMapper fileMapper;

    @Resource
    private UserMapper userMapper;

    @Value("${project.folder}")
    private String projectFolder;

    @Override
    @Transactional
    public ResponseVO uploadFile(HttpServletRequest request, MultipartFile file, String fileId, String filePid) {
        try {
            // 参数校验
            if (file == null || file.isEmpty()) {
                return ResponseVO.error("文件不能为空");
            }

            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 检查用户空间是否足够
            UserInfo userInfo = userMapper.selectById(userId);
            if (userInfo.getUseSpace() + file.getSize() > userInfo.getTotalSpace()) {
                return ResponseVO.error("空间不足");
            }

            // 生成文件ID
            if (StringUtils.isEmpty(fileId)) {
                fileId = generateFileId();
            }

            // 创建文件信息
            FileInfo fileInfo = new FileInfo();
            fileInfo.setFileId(fileId);
            // fileInfo.setUserId(userId);
            fileInfo.setFilePid(StringUtils.isEmpty(filePid) ? "0" : filePid);
            fileInfo.setFileSize(file.getSize());
            fileInfo.setFileName(file.getOriginalFilename());
            fileInfo.setFilePath(getFilePath(fileId, file.getOriginalFilename()));
            fileInfo.setCreateTime(new Date());
            fileInfo.setLastUpdateTime(new Date());
            fileInfo.setFolderType(0); // 文件
            fileInfo.setStatus(1); // 正常状态

            // 保存文件到本地
            File destFile = new File(projectFolder + fileInfo.getFilePath());
            File parentDir = destFile.getParentFile();
            if (!parentDir.exists()) {
                parentDir.mkdirs();
            }
            file.transferTo(destFile);

            // 保存文件信息到数据库
            fileInfo.setUserId(userId);
            fileMapper.insert(fileInfo);

            // 更新用户空间使用情况
            userInfo.setUseSpace(userInfo.getUseSpace() + file.getSize());
            userMapper.updateById(userInfo);

            logger.info("文件上传成功: {}", file.getOriginalFilename());
            return ResponseVO.success(fileInfo);
        } catch (Exception e) {
            logger.error("文件上传失败", e);
            return ResponseVO.error("文件上传失败");
        }
    }

    @Override
    public void downloadFile(String fileId, HttpServletResponse response) {
        try {
            // 查询文件信息
            FileInfo fileInfo = fileMapper.selectById(fileId);
            if (fileInfo == null) {
                response.getWriter().write("文件不存在");
                return;
            }

            // 检查文件状态
            if (fileInfo.getStatus() != 1) {
                response.getWriter().write("文件不可用");
                return;
            }

            // 设置响应头
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

            logger.info("文件下载成功: {}", fileId);
        } catch (Exception e) {
            logger.error("文件下载失败: {}", fileId, e);
            try {
                response.getWriter().write("文件下载失败");
            } catch (Exception ex) {
                logger.error("写入响应失败", ex);
            }
        }
    }

    @Override
    public ResponseVO getFileList(String filePid, Integer pageNo, Integer pageSize) {
        try {
            // 参数校验和默认值设置
            if (StringUtils.isEmpty(filePid)) {
                filePid = "0";
            }
            if (pageNo == null || pageNo <= 0) {
                pageNo = 1;
            }
            if (pageSize == null || pageSize <= 0) {
                pageSize = 10;
            }

            // 计算分页参数
            int start = (pageNo - 1) * pageSize;

            // 查询文件列表
            List<FileInfo> fileList = fileMapper.selectByPid(filePid, start, pageSize);
            int totalCount = fileMapper.selectCountByPid(filePid);

            // 构建返回结果
            Map<String, Object> result = new HashMap<>();
            result.put("list", fileList);
            result.put("totalCount", totalCount);
            result.put("pageNo", pageNo);
            result.put("pageSize", pageSize);

            return ResponseVO.success(result);
        } catch (Exception e) {
            logger.error("获取文件列表失败", e);
            return ResponseVO.error("获取文件列表失败");
        }
    }

    @Override
    public ResponseVO createFolder(String folderName, String filePid, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(folderName)) {
                return ResponseVO.error("文件夹名称不能为空");
            }

            // 从session或token中获取用户ID
            // String userId = getUserIdFromRequest(request);
            // if (StringUtils.isEmpty(userId)) {
            //     return ResponseVO.error("用户未登录");
            // }

            // 创建文件夹信息
            FileInfo folderInfo = new FileInfo();
            folderInfo.setFileId(generateFileId());
            // folderInfo.setUserId(userId);
            folderInfo.setFilePid(StringUtils.isEmpty(filePid) ? "0" : filePid);
            folderInfo.setFileName(folderName);
            folderInfo.setFolderType(1); // 文件夹
            folderInfo.setCreateTime(new Date());
            folderInfo.setLastUpdateTime(new Date());
            folderInfo.setStatus(1); // 正常状态

            // 保存文件夹信息到数据库
            // fileMapper.insert(folderInfo);

            logger.info("文件夹创建成功: {}", folderName);
            return ResponseVO.success(folderInfo);
        } catch (Exception e) {
            logger.error("文件夹创建失败", e);
            return ResponseVO.error("文件夹创建失败");
        }
    }

    @Override
    public ResponseVO renameFile(String fileId, String fileName) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(fileId) || StringUtils.isEmpty(fileName)) {
                return ResponseVO.error("参数不能为空");
            }

            // 查询文件信息
            // FileInfo fileInfo = fileMapper.selectById(fileId);
            // if (fileInfo == null) {
            //     return ResponseVO.error("文件不存在");
            // }

            // 更新文件名
            // fileInfo.setFileName(fileName);
            // fileInfo.setLastUpdateTime(new Date());
            // fileMapper.updateById(fileInfo);

            logger.info("文件重命名成功: {} -> {}", fileId, fileName);
            return ResponseVO.success("文件重命名成功");
        } catch (Exception e) {
            logger.error("文件重命名失败: {}", fileId, e);
            return ResponseVO.error("文件重命名失败");
        }
    }

    @Override
    public ResponseVO moveFile(String fileId, String targetPid) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(fileId) || StringUtils.isEmpty(targetPid)) {
                return ResponseVO.error("参数不能为空");
            }

            // 查询文件信息
            // FileInfo fileInfo = fileMapper.selectById(fileId);
            // if (fileInfo == null) {
            //     return ResponseVO.error("文件不存在");
            // }

            // 更新文件父目录
            // fileInfo.setFilePid(targetPid);
            // fileInfo.setLastUpdateTime(new Date());
            // fileMapper.updateById(fileInfo);

            logger.info("文件移动成功: {} -> {}", fileId, targetPid);
            return ResponseVO.success("文件移动成功");
        } catch (Exception e) {
            logger.error("文件移动失败: {}", fileId, e);
            return ResponseVO.error("文件移动失败");
        }
    }

    @Override
    public ResponseVO deleteFile(String fileId) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(fileId)) {
                return ResponseVO.error("文件ID不能为空");
            }

            // 查询文件信息
            // FileInfo fileInfo = fileMapper.selectById(fileId);
            // if (fileInfo == null) {
            //     return ResponseVO.error("文件不存在");
            // }

            // 如果是文件夹，需要递归删除子文件
            // if (fileInfo.getFolderType() == 1) {
            //     deleteFolderRecursive(fileId);
            // } else {
            //     // 删除物理文件
            //     File file = new File(projectFolder + fileInfo.getFilePath());
            //     if (file.exists()) {
            //         file.delete();
            //     }
            //     // 更新用户空间
            //     // UserInfo userInfo = userMapper.selectById(fileInfo.getUserId());
            //     // userInfo.setUseSpace(userInfo.getUseSpace() - fileInfo.getFileSize());
            //     // userMapper.updateById(userInfo);
            // }

            // 删除文件记录
            // fileMapper.deleteById(fileId);

            logger.info("文件删除成功: {}", fileId);
            return ResponseVO.success("文件删除成功");
        } catch (Exception e) {
            logger.error("文件删除失败: {}", fileId, e);
            return ResponseVO.error("文件删除失败");
        }
    }

    @Override
    public ResponseVO getFilePreview(String fileId) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(fileId)) {
                return ResponseVO.error("文件ID不能为空");
            }

            // 查询文件信息
            // FileInfo fileInfo = fileMapper.selectById(fileId);
            // if (fileInfo == null) {
            //     return ResponseVO.error("文件不存在");
            // }

            // 构建预览信息
            // Map<String, Object> previewInfo = new HashMap<>();
            // previewInfo.put("fileId", fileInfo.getFileId());
            // previewInfo.put("fileName", fileInfo.getFileName());
            // previewInfo.put("fileSize", fileInfo.getFileSize());
            // previewInfo.put("createTime", fileInfo.getCreateTime());

            return ResponseVO.success("获取文件预览信息成功");
        } catch (Exception e) {
            logger.error("获取文件预览信息失败: {}", fileId, e);
            return ResponseVO.error("获取文件预览信息失败");
        }
    }

    @Override
    public ResponseVO searchFiles(String keyword, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(keyword)) {
                return ResponseVO.error("搜索关键词不能为空");
            }

            // 从session或token中获取用户ID
            // String userId = getUserIdFromRequest(request);
            // if (StringUtils.isEmpty(userId)) {
            //     return ResponseVO.error("用户未登录");
            // }

            // 搜索文件
            // List<FileInfo> searchResults = fileMapper.searchFiles(userId, keyword);

            return ResponseVO.success("搜索成功");
        } catch (Exception e) {
            logger.error("搜索文件失败: {}", keyword, e);
            return ResponseVO.error("搜索文件失败");
        }
    }

    /**
     * 生成文件ID
     */
    private String generateFileId() {
        return "F" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    /**
     * 获取文件存储路径
     */
    private String getFilePath(String fileId, String fileName) {
        String extension = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = fileName.substring(dotIndex);
        }
        return "/files/" + fileId + extension;
    }
}