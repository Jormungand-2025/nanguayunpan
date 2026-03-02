package com.nanguayunpan.service.impl;

import com.nanguayunpan.entity.UserInfo;
import com.nanguayunpan.entity.vo.ResponseVO;
import com.nanguayunpan.mapper.UserMapper;
import com.nanguayunpan.service.UserService;
import com.nanguayunpan.utils.JwtUtil;
import com.nanguayunpan.utils.ServiceUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 */
@Service("userService")
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private UserMapper userMapper;

    @Resource
    private JavaMailSender javaMailSender;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${admin.emails}")
    private String adminEmails;

    @Override
    @Transactional
    public ResponseVO register(String email, String nickName, String password, String checkCode) {
        try {
            logger.info("开始用户注册流程: {}", email);
            
            // 参数校验
            if (StringUtils.isEmpty(email) || StringUtils.isEmpty(nickName) ||
                    StringUtils.isEmpty(password) || StringUtils.isEmpty(checkCode)) {
                logger.warn("注册参数为空: email={}, nickName={}", email, nickName);
                return ResponseVO.error("参数不能为空");
            }

            // 验证邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                logger.warn("邮箱格式不正确: {}", email);
                return ResponseVO.error("邮箱格式不正确");
            }

            // 验证验证码
            logger.info("开始验证验证码: {}", email);
            boolean valid = validateCheckCode(email, checkCode);
            if (!valid) {
                logger.warn("验证码验证失败: {}", email);
                return ResponseVO.error("验证码错误或已过期");
            }
            logger.info("验证码验证成功: {}", email);

            // 检查邮箱是否已注册
            logger.info("检查邮箱是否已注册: {}", email);
            UserInfo existingUser = userMapper.selectByEmail(email);
            if (existingUser != null) {
                logger.warn("邮箱已注册: {}", email);
                return ResponseVO.error("该邮箱已注册");
            }
            logger.info("邮箱可用: {}", email);

            // 创建用户
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(generateUserId());
            userInfo.setEmail(email);
            userInfo.setNickName(nickName);
            userInfo.setPassword(password); // 实际应用中需要加密
            userInfo.setJoinTime(new Date());
            userInfo.setLastLoginTime(new Date());
            userInfo.setStatus(1); // 正常状态
            userInfo.setUseSpace(0L);
            userInfo.setTotalSpace(1024L * 1024 * 1024); // 默认1GB空间

            logger.info("准备保存用户信息: {}", email);
            
            // 保存用户
            int result = userMapper.insert(userInfo);
            if (result <= 0) {
                logger.error("用户保存失败: {}", email);
                return ResponseVO.error("注册失败");
            }

            logger.info("用户注册成功: {}", email);
            return ResponseVO.success("注册成功");
        } catch (Exception e) {
            logger.error("用户注册失败: {}", email, e);
            // 打印详细的错误堆栈
            e.printStackTrace();
            return ResponseVO.error("注册失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseVO login(String email, String password, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password)) {
                return ResponseVO.error("邮箱或密码不能为空");
            }

            // 查询用户
            UserInfo userInfo = userMapper.selectByEmail(email);
            if (userInfo == null) {
                return ResponseVO.error("用户不存在");
            }

            // 验证密码
            if (!password.equals(userInfo.getPassword())) {
                return ResponseVO.error("密码错误");
            }

            // 检查用户状态
            if (userInfo.getStatus() != 1) {
                return ResponseVO.error("账户已被禁用");
            }

            // 更新最后登录时间
            userInfo.setLastLoginTime(new Date());
            userMapper.updateById(userInfo);

            // 设置用户ID到Session中（兼容原有逻辑）
            request.getSession().setAttribute("userId", userInfo.getUserId());
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(userInfo.getUserId(), userInfo.getEmail());
            
            // 返回用户信息（排除密码）和JWT令牌
            userInfo.setPassword(null);
            
            Map<String, Object> result = new HashMap<>();
            result.put("userInfo", userInfo);
            result.put("token", token);
            result.put("expiresIn", 86400); // 24小时，单位秒

            logger.info("用户登录成功: {}", email);
            return ResponseVO.success("登录成功", result);
        } catch (Exception e) {
            logger.error("用户登录失败: {}", email, e);
            return ResponseVO.error("登录失败");
        }
    }

    @Override
    public ResponseVO getUserInfo(HttpServletRequest request) {
        try {
            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 查询用户信息
            UserInfo userInfo = userMapper.selectById(userId);
            if (userInfo == null) {
                return ResponseVO.error("用户不存在");
            }

            // 排除敏感信息
            userInfo.setPassword(null);

            return ResponseVO.success(userInfo);
        } catch (Exception e) {
            logger.error("获取用户信息失败", e);
            return ResponseVO.error("获取用户信息失败");
        }
    }

    @Override
    public ResponseVO getUseSpace(HttpServletRequest request) {
        try {
            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 查询用户空间使用情况
            UserInfo userInfo = userMapper.selectById(userId);
            if (userInfo == null) {
                return ResponseVO.error("用户不存在");
            }

            // 构建空间使用信息
            Map<String, Object> spaceInfo = new HashMap<>();
            spaceInfo.put("useSpace", userInfo.getUseSpace());
            spaceInfo.put("totalSpace", userInfo.getTotalSpace());
            spaceInfo.put("freeSpace", userInfo.getTotalSpace() - userInfo.getUseSpace());

            return ResponseVO.success(spaceInfo);
        } catch (Exception e) {
            logger.error("获取空间使用情况失败", e);
            return ResponseVO.error("获取空间使用情况失败");
        }
    }

    @Override
    public ResponseVO resetPassword(String email, String password, String checkCode) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(email) || StringUtils.isEmpty(password) || StringUtils.isEmpty(checkCode)) {
                return ResponseVO.error("参数不能为空");
            }

            // 验证验证码
            boolean valid = validateCheckCode(email, checkCode);
            if (!valid) {
                return ResponseVO.error("验证码错误或已过期");
            }

            // 查询用户
            UserInfo userInfo = userMapper.selectByEmail(email);
            if (userInfo == null) {
                return ResponseVO.error("用户不存在");
            }

            // 更新密码
            userInfo.setPassword(password); // 实际应用中需要加密
            userMapper.updateById(userInfo);

            logger.info("密码重置成功: {}", email);
            return ResponseVO.success("密码重置成功");
        } catch (Exception e) {
            logger.error("密码重置失败: {}", email, e);
            return ResponseVO.error("密码重置失败");
        }
    }

    @Override
    public ResponseVO updateUserInfo(String nickName, HttpServletRequest request) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(nickName)) {
                return ResponseVO.error("昵称不能为空");
            }

            // 从session或token中获取用户ID
            String userId = ServiceUtils.getUserIdFromRequest(request);
            if (StringUtils.isEmpty(userId)) {
                return ResponseVO.error("用户未登录");
            }

            // 更新用户信息
            UserInfo userInfo = new UserInfo();
            userInfo.setUserId(userId);
            userInfo.setNickName(nickName);
            userMapper.updateById(userInfo);

            logger.info("用户信息更新成功");
            return ResponseVO.success("用户信息更新成功");
        } catch (Exception e) {
            logger.error("用户信息更新失败", e);
            return ResponseVO.error("用户信息更新失败");
        }
    }

    @Override
    public ResponseVO sendEmailCode(String email, Integer type) {
        try {
            // 参数校验
            if (StringUtils.isEmpty(email)) {
                return ResponseVO.error("邮箱不能为空");
            }

            // 验证邮箱格式
            if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseVO.error("邮箱格式不正确");
            }

            // 生成验证码
            String checkCode = generateCheckCode();

            // 发送邮件
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);

            if (type == 0) {
                message.setSubject("南呱云盘 - 注册验证码");
                message.setText("您的注册验证码是：" + checkCode + "，有效期为5分钟。");
            } else if (type == 1) {
                message.setSubject("南呱云盘 - 重置密码验证码");
                message.setText("您的重置密码验证码是：" + checkCode + "，有效期为5分钟。");
            } else {
                return ResponseVO.error("不支持的操作类型");
            }

            javaMailSender.send(message);

            // 保存验证码到Redis（需要实现）
            redisTemplate.opsForValue().set("checkCode:" + email, checkCode, 5, TimeUnit.MINUTES);

            logger.info("验证码发送成功: {}", email);
            return ResponseVO.success("验证码发送成功");
        } catch (Exception e) {
            logger.error("验证码发送失败: {}", email, e);
            return ResponseVO.error("验证码发送失败");
        }
    }

    /**
     * 生成用户ID
     */
    private String generateUserId() {
        return "U" + System.currentTimeMillis() + new Random().nextInt(1000);
    }

    /**
     * 生成验证码
     */
    private String generateCheckCode() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }

    /**
     * 验证邮箱验证码
     */
    private boolean validateCheckCode(String email, String checkCode) {
        try {
            // 从Redis中获取验证码
            String storedCode = redisTemplate.opsForValue().get("checkCode:" + email);
            if (StringUtils.isEmpty(storedCode)) {
                return false;
            }
            
            // 验证验证码是否匹配
            if (!storedCode.equals(checkCode)) {
                return false;
            }
            
            // 验证成功后删除验证码
            redisTemplate.delete("checkCode:" + email);
            return true;
        } catch (Exception e) {
            logger.error("验证验证码失败: {}", email, e);
            return false;
        }
    }

    @Override
    public ResponseVO logout(HttpServletRequest request) {
        try {
            // 从session中移除用户信息
            request.getSession().removeAttribute("userId");
            
            // 可选：清除其他相关的session属性
            request.getSession().removeAttribute("userInfo");
            
            // 可选：使session失效
            request.getSession().invalidate();
            
            logger.info("用户退出登录成功");
            return ResponseVO.success("退出登录成功");
        } catch (Exception e) {
            logger.error("退出登录失败", e);
            return ResponseVO.error("退出登录失败");
        }
    }
}