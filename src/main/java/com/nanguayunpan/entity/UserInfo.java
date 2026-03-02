package com.nanguayunpan.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;

public class UserInfo {
    private String userId;
    private String nickName;
    private String email;
    @JsonIgnore
    private String password;
    private Date joinTime;
    private Date lastLoginTime;
    private Integer status;
    private Long useSpace;
    private Long totalSpace;

    // getter和setter方法
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickName) { this.nickName = nickName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Date getJoinTime() { return joinTime; }
    public void setJoinTime(Date joinTime) { this.joinTime = joinTime; }

    public Date getLastLoginTime() { return lastLoginTime; }
    public void setLastLoginTime(Date lastLoginTime) { this.lastLoginTime = lastLoginTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getUseSpace() { return useSpace; }
    public void setUseSpace(Long useSpace) { this.useSpace = useSpace; }

    public Long getTotalSpace() { return totalSpace; }
    public void setTotalSpace(Long totalSpace) { this.totalSpace = totalSpace; }
}