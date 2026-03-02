package com.nanguayunpan.service;

import com.nanguayunpan.entity.vo.ResponseVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 文件服务接口
 */
public interface FileService {

    /**
     * 文件上传
     */
    ResponseVO uploadFile(HttpServletRequest request, MultipartFile file, String fileId, String filePid);

    /**
     * 文件下载
     */
    void downloadFile(String fileId, HttpServletResponse response);

    /**
     * 获取文件列表
     */
    ResponseVO getFileList(String filePid, Integer pageNo, Integer pageSize, HttpServletRequest request);

    /**
     * 创建文件夹
     */
    ResponseVO createFolder(String folderName, String filePid, HttpServletRequest request);

    /**
     * 重命名文件/文件夹
     */
    ResponseVO renameFile(String fileId, String fileName);

    /**
     * 移动文件/文件夹
     */
    ResponseVO moveFile(String fileId, String targetPid);

    /**
     * 删除文件/文件夹
     */
    ResponseVO deleteFile(String fileId);

    /**
     * 获取文件预览信息
     */
    ResponseVO getFilePreview(String fileId);

    /**
     * 搜索文件
     */
    ResponseVO searchFiles(String keyword, HttpServletRequest request);
}