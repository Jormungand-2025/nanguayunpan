package com.nanguayunpan.mapper;

import com.nanguayunpan.entity.ShareInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 分享数据访问层接口
 */
@Mapper
public interface ShareMapper {

    /**
     * 根据分享ID查询分享信息
     */
    ShareInfo selectById(@Param("shareId") String shareId);

    /**
     * 根据分享码查询分享信息
     */
    ShareInfo selectByShareCode(@Param("shareCode") String shareCode);

    /**
     * 插入分享信息
     */
    int insert(ShareInfo shareInfo);

    /**
     * 更新分享信息
     */
    int updateById(ShareInfo shareInfo);

    /**
     * 根据分享ID删除分享
     */
    int deleteById(@Param("shareId") String shareId);

    /**
     * 根据用户ID查询分享列表
     */
    List<ShareInfo> selectByUserId(@Param("userId") String userId);

    /**
     * 根据文件ID查询分享信息
     */
    List<ShareInfo> selectByFileId(@Param("fileId") String fileId);

    /**
     * 根据状态查询分享列表
     */
    List<ShareInfo> selectByStatus(@Param("status") Integer status);

    /**
     * 统计用户分享数量
     */
    int selectCountByUserId(@Param("userId") String userId);

    /**
     * 分页查询分享列表
     */
    List<ShareInfo> selectPage(@Param("start") int start, @Param("pageSize") int pageSize);

    /**
     * 查询过期分享
     */
    List<ShareInfo> selectExpiredShares();

    /**
     * 批量更新分享状态
     */
    int batchUpdateStatus(@Param("shareIds") List<String> shareIds, @Param("status") Integer status);

    /**
     * 根据分享码和用户ID查询分享信息
     */
    ShareInfo selectByShareCodeAndUserId(@Param("shareCode") String shareCode, @Param("userId") String userId);

    /**
     * 统计分享总数
     */
    int selectCount();

    /**
     * 获取热门分享（按查看次数排序）
     */
    List<ShareInfo> selectPopularShares(@Param("limit") int limit);
}