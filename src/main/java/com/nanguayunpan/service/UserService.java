package com.nanguayunpan.service;

import com.nanguayunpan.entity.vo.ResponseVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 用户注册
     */
    ResponseVO register(String email, String nickName, String password, String checkCode);

    /**
     * 用户登录
     */
    ResponseVO login(String email, String password, HttpServletRequest request);

    /**
     * 获取用户信息
     */
    ResponseVO getUserInfo(HttpServletRequest request);

    /**
     * 获取用户空间使用情况
     */
    ResponseVO getUseSpace(HttpServletRequest request);

    /**
     * 重置密码
     */
    ResponseVO resetPassword(String email, String password, String checkCode);

    /**
     * 更新用户信息
     */
    ResponseVO updateUserInfo(String nickName, HttpServletRequest request);

    /**
     * 发送邮箱验证码
     */
    ResponseVO sendEmailCode(String email, Integer type);

    /**
     * 用户退出登录
     */
    ResponseVO logout(HttpServletRequest request);
}