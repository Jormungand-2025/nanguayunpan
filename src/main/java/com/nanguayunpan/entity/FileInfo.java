package com.nanguayunpan.entity;

import java.util.Date;

public class FileInfo {
    private String fileId;
    private String userId;
    private String fileMd5;
    private String filePid;
    private Long fileSize;
    private String fileName;
    private String fileCover;
    private String filePath;
    private Date createTime;
    private Date lastUpdateTime;
    private Integer folderType;
    private Integer fileCategory;
    private Integer fileType;
    private Integer status;

    // getter和setter方法
    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFileMd5() { return fileMd5; }
    public void setFileMd5(String fileMd5) { this.fileMd5 = fileMd5; }

    public String getFilePid() { return filePid; }
    public void setFilePid(String filePid) { this.filePid = filePid; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileCover() { return fileCover; }
    public void setFileCover(String fileCover) { this.fileCover = fileCover; }

    public String getFilePath() { return filePath; }
    public void setFilePath(String filePath) { this.filePath = filePath; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(Date lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }

    public Integer getFolderType() { return folderType; }
    public void setFolderType(Integer folderType) { this.folderType = folderType; }

    public Integer getFileCategory() { return fileCategory; }
    public void setFileCategory(Integer fileCategory) { this.fileCategory = fileCategory; }

    public Integer getFileType() { return fileType; }
    public void setFileType(Integer fileType) { this.fileType = fileType; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}