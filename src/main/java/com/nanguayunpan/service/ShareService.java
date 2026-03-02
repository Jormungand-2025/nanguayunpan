package com.nanguayunpan.service;

import com.nanguayunpan.entity.vo.ResponseVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 分享服务接口
 */
public interface ShareService {

    /**
     * 创建文件分享
     */
    ResponseVO createShare(String fileId, Integer shareType, Integer expireDays, String password, HttpServletRequest request);

    /**
     * 获取分享信息
     */
    ResponseVO getShareInfo(String shareCode);

    /**
     * 验证分享密码
     */
    ResponseVO validateSharePassword(String shareCode, String password);

    /**
     * 获取分享文件列表
     */
    ResponseVO getShareFileList(String shareCode, String filePid);

    /**
     * 下载分享文件
     */
    void downloadShareFile(String shareCode, String fileId, HttpServletRequest request);

    /**
     * 取消分享
     */
    ResponseVO cancelShare(String shareCode, HttpServletRequest request);

    /**
     * 获取我的分享列表
     */
    ResponseVO getMyShareList(HttpServletRequest request);

    /**
     * 检查分享是否有效
     */
    ResponseVO checkShareValid(String shareCode);

    /**
     * 更新分享设置
     */
    ResponseVO updateShareSettings(String shareCode, Integer expireDays, String password, HttpServletRequest request);

    /**
     * 获取分享统计信息
     */
    ResponseVO getShareStatistics(String shareCode, HttpServletRequest request);
}