package com.kaiming.xiaohongshu.data.align.domain.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName: DeleteMapper
 * Package: com.kaiming.xiaohongshu.data.align.domain.mapper
 * Description:
 *
 * @Auther gongkaiming
 * @Create 2025/7/12 20:58
 * @Version 1.0
 */
public interface DeleteMapper {

    /**
     * 日增量表：关注数计数变更 - 批量删除
     *
     * @param userIds
     */
    void batchDeleteDataAlignFollowingCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                     @Param("userIds") List<Long> userIds);

    /**
     * 日增量表：笔记点赞计数变更 - 批量删除
     */
    void batchDeleteDataAlignNoteLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix,
                                                    @Param("noteIds") List<Long> noteIds);

    /**
     * 日增量表：笔记收藏计数变更 - 批量删除
     *
     * @param tableNameSuffix
     * @param noteIds
     */
    void batchDeleteDataAlignNoteCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("noteIds") List<Long> noteIds);

    /**
     * 日增量表：粉丝数计数变更 - 批量删除
     *
     * @param tableNameSuffix
     * @param userIds
     */
    void batchDeleteDataAlignFansCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") List<Long> userIds);

    /**
     * 日增量表：用户点赞数计数变更 - 批量删除
     *
     * @param tableNameSuffix
     * @param userIds
     */
    void batchDeleteDataAlignUserLikeCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") List<Long> userIds);

    /**
     * 日增量表：用户收藏数计数变更 - 批量删除
     *
     * @param tableNameSuffix
     * @param userIds
     */
    void batchDeleteDataAlignUserCollectCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") List<Long> userIds);

    /**
     * 日增量表：笔记发布数计数变更 - 批量删除
     *
     * @param tableNameSuffix
     * @param userIds
     */
    void batchDeleteDataAlignNotePublishCountTempTable(@Param("tableNameSuffix") String tableNameSuffix, @Param("userId") List<Long> userIds);
}
