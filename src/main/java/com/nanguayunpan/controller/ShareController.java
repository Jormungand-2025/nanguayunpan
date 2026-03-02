package com.nanguayunpan.controller;

import com.nanguayunpan.entity.vo.ResponseVO;
import com.nanguayunpan.service.ShareService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 分享控制器
 */
@RestController
@RequestMapping("/share")
public class ShareController {

    @Resource
    private ShareService shareService;

    /**
     * 创建文件分享
     */
    @PostMapping("/create")
    public ResponseVO createShare(@RequestParam String fileId,
                                  @RequestParam(required = false) Integer shareType,
                                  @RequestParam(required = false) Integer expireDays,
                                  @RequestParam(required = false) String password,
                                  HttpServletRequest request) {
        return shareService.createShare(fileId, shareType, expireDays, password, request);
    }

    /**
     * 获取分享信息
     */
    @GetMapping("/info/{shareCode}")
    public ResponseVO getShareInfo(@PathVariable String shareCode) {
        return shareService.getShareInfo(shareCode);
    }

    /**
     * 验证分享密码
     */
    @PostMapping("/validate")
    public ResponseVO validateSharePassword(@RequestParam String shareCode,
                                            @RequestParam String password) {
        return shareService.validateSharePassword(shareCode, password);
    }

    /**
     * 获取分享文件列表
     */
    @GetMapping("/files/{shareCode}")
    public ResponseVO getShareFileList(@PathVariable String shareCode,
                                       @RequestParam(required = false) String filePid) {
        return shareService.getShareFileList(shareCode, filePid);
    }

    /**
     * 下载分享文件
     */
    @GetMapping("/download/{shareCode}/{fileId}")
    public void downloadShareFile(@PathVariable String shareCode,
                                  @PathVariable String fileId,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        request.setAttribute("response", response);
        shareService.downloadShareFile(shareCode, fileId, request);
    }

    /**
     * 取消分享
     */
    @PostMapping("/cancel")
    public ResponseVO cancelShare(@RequestParam String shareCode,
                                  HttpServletRequest request) {
        return shareService.cancelShare(shareCode, request);
    }

    /**
     * 获取我的分享列表
     */
    @GetMapping("/my")
    public ResponseVO getMyShareList(HttpServletRequest request) {
        return shareService.getMyShareList(request);
    }

    /**
     * 检查分享是否有效
     */
    @GetMapping("/check/{shareCode}")
    public ResponseVO checkShareValid(@PathVariable String shareCode) {
        return shareService.checkShareValid(shareCode);
    }

    /**
     * 更新分享设置
     */
    @PostMapping("/update")
    public ResponseVO updateShareSettings(@RequestParam String shareCode,
                                          @RequestParam(required = false) Integer expireDays,
                                          @RequestParam(required = false) String password,
                                          HttpServletRequest request) {
        return shareService.updateShareSettings(shareCode, expireDays, password, request);
    }

    /**
     * 获取分享统计信息
     */
    @GetMapping("/statistics/{shareCode}")
    public ResponseVO getShareStatistics(@PathVariable String shareCode,
                                         HttpServletRequest request) {
        return shareService.getShareStatistics(shareCode, request);
    }

    /**
     * 公开分享页面（用于直接访问分享链接）
     */
    @GetMapping("/{shareCode}")
    public ResponseVO sharePage(@PathVariable String shareCode) {
        return shareService.getShareInfo(shareCode);
    }
}