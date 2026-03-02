package com.nanguayunpan.mapper;

import com.nanguayunpan.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文件数据访问层接口
 */
@Mapper
public interface FileMapper {

    /**
     * 根据文件ID查询文件信息
     */
    FileInfo selectById(@Param("fileId") String fileId);

    /**
     * 根据父目录ID查询文件列表
     */
    List<FileInfo> selectByPid(@Param("filePid") String filePid,
                               @Param("start") int start,
                               @Param("pageSize") int pageSize);

    /**
     * 根据父目录ID统计文件数量
     */
    int selectCountByPid(@Param("filePid") String filePid);

    /**
     * 根据用户ID和父目录ID查询文件列表
     */
    List<FileInfo> selectByUserIdAndPid(@Param("userId") String userId,
                                       @Param("filePid") String filePid,
                                       @Param("start") int start,
                                       @Param("pageSize") int pageSize);

    /**
     * 根据用户ID和父目录ID统计文件数量
     */
    int selectCountByUserIdAndPid(@Param("userId") String userId,
                                  @Param("filePid") String filePid);

    /**
     * 插入文件信息
     */
    int insert(FileInfo fileInfo);

    /**
     * 更新文件信息
     */
    int updateById(FileInfo fileInfo);

    /**
     * 根据文件ID删除文件
     */
    int deleteById(@Param("fileId") String fileId);

    /**
     * 根据用户ID查询文件列表
     */
    List<FileInfo> selectByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID和文件类型查询文件
     */
    List<FileInfo> selectByUserIdAndType(@Param("userId") String userId,
                                         @Param("folderType") Integer folderType);

    /**
     * 根据用户ID和文件状态查询文件
     */
    List<FileInfo> selectByUserIdAndStatus(@Param("userId") String userId,
                                           @Param("status") Integer status);

    /**
     * 根据父目录ID递归查询所有子文件
     */
    List<FileInfo> selectChildrenByPid(@Param("filePid") String filePid);

    /**
     * 根据用户ID和文件名搜索文件
     */
    List<FileInfo> searchFiles(@Param("userId") String userId,
                               @Param("keyword") String keyword);

    /**
     * 根据文件MD5查询文件信息（用于秒传功能）
     */
    FileInfo selectByMd5(@Param("fileMd5") String fileMd5);

    /**
     * 根据用户ID统计文件数量
     */
    int selectCountByUserId(@Param("userId") String userId);

    /**
     * 根据用户ID统计已使用空间
     */
    Long selectTotalUseSpaceByUserId(@Param("userId") String userId);

    /**
     * 批量更新文件状态
     */
    int batchUpdateStatus(@Param("fileIds") List<String> fileIds,
                          @Param("status") Integer status);

    /**
     * 根据文件路径查询文件信息
     */
    FileInfo selectByFilePath(@Param("filePath") String filePath);

    /**
     * 根据文件类型统计文件数量
     */
    int selectCountByFileType(@Param("fileType") Integer fileType);

    /**
     * 获取用户最近上传的文件
     */
    List<FileInfo> selectRecentFilesByUserId(@Param("userId") String userId,
                                             @Param("limit") int limit);
}