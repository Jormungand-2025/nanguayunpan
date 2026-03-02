package com.nanguayunpan.controller;

import com.nanguayunpan.entity.vo.ResponseVO;
import com.nanguayunpan.service.FileService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController("fileController")
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    /**
     * 文件上传
     */
    @RequestMapping("/upload")
    public ResponseVO uploadFile(HttpServletRequest request,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam String fileId,
                                 @RequestParam(required = false) String filePid) {
        return fileService.uploadFile(request, file, fileId, filePid);
    }

    /**
     * 文件下载
     */
    @RequestMapping("/download/{fileId}")
    public void downloadFile(@PathVariable("fileId") String fileId,
                             HttpServletResponse response) {
        fileService.downloadFile(fileId, response);
    }

    /**
     * 获取文件列表
     */
    @RequestMapping("/list")
    public ResponseVO getFileList(@RequestParam String filePid,
                                  @RequestParam(required = false) Integer pageNo,
                                  @RequestParam(required = false) Integer pageSize) {
        return fileService.getFileList(filePid, pageNo, pageSize);
    }

    /**
     * 创建文件夹
     */
    @RequestMapping("/createFolder")
    public ResponseVO createFolder(@RequestParam String fileName,
                                   @RequestParam String filePid,
                                   HttpServletRequest request) {
        return fileService.createFolder(fileName, filePid, request);
    }

    /**
     * 重命名文件
     */
    @RequestMapping("/rename")
    public ResponseVO renameFile(@RequestParam String fileId,
                                 @RequestParam String fileName) {
        return fileService.renameFile(fileId, fileName);
    }

    /**
     * 删除文件
     */
    @RequestMapping("/delete")
    public ResponseVO deleteFile(@RequestParam String fileIds) {
        return fileService.deleteFile(fileIds);
    }
}
