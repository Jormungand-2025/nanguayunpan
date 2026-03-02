package com.nanguayunpan.controller;

import com.nanguayunpan.entity.vo.ResponseVO;
import com.nanguayunpan.service.UserService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@RestController("userController")
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ResponseVO register(@RequestParam String email,
                               @RequestParam String nickName,
                               @RequestParam String password,
                               @RequestParam String checkCode) {
        return userService.register(email, nickName, password, checkCode);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ResponseVO login(@RequestParam String email,
                            @RequestParam String password,
                            HttpServletRequest request) {
        return userService.login(email, password, request);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/getUserInfo")
    public ResponseVO getUserInfo(HttpServletRequest request) {
        return userService.getUserInfo(request);
    }

    /**
     * 获取用户空间使用情况
     */
    @GetMapping("/getUseSpace")
    public ResponseVO getUseSpace(HttpServletRequest request) {
        return userService.getUseSpace(request);
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/sendEmailCode")
    public ResponseVO sendEmailCode(@RequestParam String email,
                                    @RequestParam Integer type) {
        return userService.sendEmailCode(email, type);
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public ResponseVO logout(HttpServletRequest request) {
        return userService.logout(request);
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseVO healthCheck() {
        return ResponseVO.success("南呱云盘服务运行正常");
    }

    /**
     * 测试端点
     */
    @GetMapping("/test")
    public ResponseVO testEndpoint() {
        return ResponseVO.success("API测试端点正常");
    }

    /**
     * Redis连接测试
     */
    @GetMapping("/redisTest")
    public ResponseVO redisTest() {
        try {
            // 测试Redis连接
            redisTemplate.opsForValue().set("test:connection", "success", 60, TimeUnit.SECONDS);
            String result = redisTemplate.opsForValue().get("test:connection");
            
            if ("success".equals(result)) {
                return ResponseVO.success("Redis连接正常");
            } else {
                return ResponseVO.error("Redis连接异常");
            }
        } catch (Exception e) {
            return ResponseVO.error("Redis连接失败: " + e.getMessage());
        }
    }
}