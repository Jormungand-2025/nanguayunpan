package com.nanguayunpan.mapper;

import com.nanguayunpan.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户数据访问层接口
 */
@Mapper
public interface UserMapper {

    /**
     * 根据用户ID查询用户信息
     */
    UserInfo selectById(@Param("userId") String userId);

    /**
     * 根据邮箱查询用户信息
     */
    UserInfo selectByEmail(@Param("email") String email);

    /**
     * 插入用户信息
     */
    int insert(UserInfo userInfo);

    /**
     * 更新用户信息
     */
    int updateById(UserInfo userInfo);

    /**
     * 根据用户ID删除用户
     */
    int deleteById(@Param("userId") String userId);

    /**
     * 查询所有用户（管理员功能）
     */
    List<UserInfo> selectAll();

    /**
     * 根据状态查询用户
     */
    List<UserInfo> selectByStatus(@Param("status") Integer status);

    /**
     * 更新用户最后登录时间
     */
    int updateLastLoginTime(@Param("userId") String userId);

    /**
     * 更新用户空间使用情况
     */
    int updateUseSpace(@Param("userId") String userId, @Param("useSpace") Long useSpace);

    /**
     * 根据昵称模糊查询用户
     */
    List<UserInfo> selectByNickNameLike(@Param("nickName") String nickName);

    /**
     * 统计用户总数
     */
    int selectCount();

    /**
     * 分页查询用户
     */
    List<UserInfo> selectPage(@Param("start") int start, @Param("pageSize") int pageSize);
}