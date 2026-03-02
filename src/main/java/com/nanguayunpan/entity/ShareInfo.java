package com.nanguayunpan.entity;

import java.util.Date;

/**
 * 分享信息实体类
 */
public class ShareInfo {
    private String shareId;
    private String fileId;
    private String userId;
    private String shareCode;
    private String shareUrl;
    private Date createTime;
    private Date expireTime;
    private Integer shareType; // 0-公开分享，1-私密分享（需要密码）
    private Integer viewCount;
    private Integer downloadCount;
    private Integer status; // 0-已失效，1-有效
    private String password; // 分享密码（私密分享时使用）

    // getter和setter方法
    public String getShareId() { return shareId; }
    public void setShareId(String shareId) { this.shareId = shareId; }

    public String getFileId() { return fileId; }
    public void setFileId(String fileId) { this.fileId = fileId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getShareCode() { return shareCode; }
    public void setShareCode(String shareCode) { this.shareCode = shareCode; }

    public String getShareUrl() { return shareUrl; }
    public void setShareUrl(String shareUrl) { this.shareUrl = shareUrl; }

    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date getExpireTime() { return expireTime; }
    public void setExpireTime(Date expireTime) { this.expireTime = expireTime; }

    public Integer getShareType() { return shareType; }
    public void setShareType(Integer shareType) { this.shareType = shareType; }

    public Integer getViewCount() { return viewCount; }
    public void setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getDownloadCount() { return downloadCount; }
    public void setDownloadCount(Integer downloadCount) { this.downloadCount = downloadCount; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}